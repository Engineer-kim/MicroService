package com.food.ordering.system.domain;

import com.food.ordering.system.domain.dto.PaymentRequest;
import com.food.ordering.system.domain.exception.PaymentApplicationServiceException;
import com.food.ordering.system.domain.mapper.PaymentDataMapper;
import com.food.ordering.system.domain.outbox.model.OrderOutboxMessage;
import com.food.ordering.system.domain.outbox.scheduler.OrderOutboxHelper;
import com.food.ordering.system.domain.ports.output.message.publisher.PaymentCancelledMessagePublisher;
import com.food.ordering.system.domain.ports.output.message.publisher.PaymentCompletedMessagePublisher;
import com.food.ordering.system.domain.ports.output.message.publisher.PaymentFailedMessagePublisher;
import com.food.ordering.system.domain.ports.output.message.publisher.PaymentResponseMessagePublisher;
import com.food.ordering.system.domain.ports.output.repository.CreditEntryRepository;
import com.food.ordering.system.domain.ports.output.repository.CreditHistoryRepository;
import com.food.ordering.system.domain.ports.output.repository.PaymentRepository;
import com.food.ordering.system.domain.valueobject.CustomerId;
import com.food.ordering.system.domain.valueobject.PaymentStatus;
import com.food.ordering.system.outbox.OutboxStatus;
import com.food.ordering.system.payment.service.domain.PaymentDomainService;
import com.food.ordering.system.payment.service.domain.entity.CreditEntry;
import com.food.ordering.system.payment.service.domain.entity.CreditHistory;
import com.food.ordering.system.payment.service.domain.entity.Payment;
import com.food.ordering.system.payment.service.domain.event.PaymentCancelledEvent;
import com.food.ordering.system.payment.service.domain.event.PaymentEvent;
import com.food.ordering.system.payment.service.domain.event.PaymentFailedEvent;
import com.food.ordering.system.payment.service.domain.exception.PaymentNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class PaymentRequestHelper {
    private final PaymentDomainService paymentDomainService;
    private final PaymentDataMapper paymentDataMapper;
    private final PaymentRepository paymentRepository;
    private final CreditEntryRepository creditEntryRepository;
    private final CreditHistoryRepository creditHistoryRepository;
    private final OrderOutboxHelper orderOutboxHelper;
    private final PaymentResponseMessagePublisher  paymentResponseMessagePublisher;


    public PaymentRequestHelper(PaymentDomainService paymentDomainService, PaymentDataMapper paymentDataMapper, PaymentRepository paymentRepository, CreditEntryRepository creditEntryRepository, CreditHistoryRepository creditHistoryRepository, OrderOutboxHelper orderOutboxHelper, PaymentResponseMessagePublisher paymentResponseMessagePublisher) {
        this.paymentDomainService = paymentDomainService;
        this.paymentDataMapper = paymentDataMapper;
        this.paymentRepository = paymentRepository;
        this.creditEntryRepository = creditEntryRepository;
        this.creditHistoryRepository = creditHistoryRepository;
        this.orderOutboxHelper = orderOutboxHelper;
        this.paymentResponseMessagePublisher = paymentResponseMessagePublisher;
    }
//    private final PaymentCompletedMessagePublisher paymentCompletedMessagePublisher;
//    private final PaymentCancelledMessagePublisher paymentCancelledMessagePublisher;
//    private final PaymentFailedMessagePublisher paymentFailedMessagePublisher;


    @Transactional
    public void persistPayment(PaymentRequest paymentRequest) {
        if(publishIfOutboxMessageProcessedForPayment(paymentRequest, PaymentStatus.COMPLETED)){
            //지불 완료된건
            log.info("해당 사가 아이디는 : {} 인데 ,이미 디비에 저장되었음", paymentRequest.getSagaId());
            return;
        }
        log.info("Received payment complete event for order id: {}", paymentRequest.getOrderId());
        Payment payment = paymentDataMapper.paymentRequestModelToPayment(paymentRequest);
        CreditEntry creditEntry = getCreditEntry(payment.getCustomerId());
        List<CreditHistory> creditHistories = getCreditHistory(payment.getCustomerId());
        List<String> failureMessages = new ArrayList<>();
        PaymentEvent paymentEvent = paymentDomainService.validateAndInitiatePayment(payment, creditEntry, creditHistories, failureMessages);
        paymentRepository.save(payment);
        persistDBObject(failureMessages, creditEntry, creditHistories, paymentEvent);

        orderOutboxHelper.saveOrderOutboxMessage(paymentDataMapper.paymentEventToOrderEventPayload(paymentEvent), paymentEvent.getPayment().getPaymentStatus(), OutboxStatus.STARTED, UUID.fromString(paymentRequest.getSagaId()));
    }

    private CreditEntry getCreditEntry(CustomerId customerId) {
        Optional<CreditEntry> creditEntry = creditEntryRepository.findByCustomerId(customerId);
        if (creditEntry.isEmpty()) {
            log.error("Could not find credit entry for customer: {}", customerId.getValue());
            throw new PaymentApplicationServiceException("Could not find credit entry for customer: " + customerId.getValue());
        }
        return creditEntry.get();
    }

    private boolean publishIfOutboxMessageProcessedForPayment(PaymentRequest paymentRequest , PaymentStatus paymentStatus) {

        Optional<OrderOutboxMessage> orderOutboxMessage = orderOutboxHelper.getCompletedOrderOutboxMessageBySagaIdAndPaymentStatus(UUID.fromString(paymentRequest.getSagaId()), paymentStatus);
        if (orderOutboxMessage.isPresent()) {
            paymentResponseMessagePublisher.publish(orderOutboxMessage.get(), orderOutboxHelper::updateOutboxMessage);
            return true;
        }
        return false;
    }

    private List<CreditHistory> getCreditHistory(CustomerId customerId) {
        Optional<List<CreditHistory>> creditHistories = creditHistoryRepository.findByCustomerId(customerId);
        if (creditHistories.isEmpty()) {
            log.error("Could not find credit history for customer: {}", customerId.getValue());
            throw new PaymentApplicationServiceException("Could not find credit history for customer: " +
                    customerId.getValue());
        }
        return creditHistories.get();
    }

    @Transactional
    public void persistCancelPayment(PaymentRequest paymentRequest) {
        if(publishIfOutboxMessageProcessedForPayment(paymentRequest, PaymentStatus.CANCELLED)){
            //지불 취소 완료된건
            log.info("해당 사가 아이디는 : {} 인데 ,이미 디비에 저장되었음", paymentRequest.getSagaId());
            return;
        }

        log.info("Received payment rollback event for order id: {}", paymentRequest.getOrderId());
        Optional<Payment> paymentResponse = paymentRepository.findByOrderId(UUID.fromString(paymentRequest.getOrderId()));
        if (paymentResponse.isEmpty()) {
            log.error("Payment with order id: {} could not be found!", paymentRequest.getOrderId());
            throw new PaymentNotFoundException("Payment with order id: " + paymentRequest.getOrderId() + " could not be found!");
        }
        Payment payment = paymentResponse.get();
        CreditEntry creditEntry = getCreditEntry(payment.getCustomerId());
        List<CreditHistory> creditHistories = getCreditHistory(payment.getCustomerId());
        List<String> failureMessages = new ArrayList<>();
        PaymentEvent paymentEvent = paymentDomainService.validateAndCancelPayment(payment, creditEntry, creditHistories, failureMessages);
        persistDBObject(failureMessages, creditEntry, creditHistories, paymentEvent);

        orderOutboxHelper.saveOrderOutboxMessage(paymentDataMapper.paymentEventToOrderEventPayload(paymentEvent), paymentEvent.getPayment().getPaymentStatus(), OutboxStatus.STARTED, UUID.fromString(paymentRequest.getSagaId()));
    }

    private PaymentEvent persistDBObject(List<String> failureMessages, CreditEntry creditEntry, List<CreditHistory> creditHistories, PaymentEvent paymentEvent) {
        if(failureMessages.isEmpty()){
            creditEntryRepository.save(creditEntry);
            //결제 내역 업데이트를 안하고 마지막요소를 가지고와서 save함 리스트이기떄문에 -1 로 마지막꺼 Save
            //업데이트 시 비지니스 로직오류가 나면 오류 처리하기가 복잡해지기때문
            creditHistoryRepository.save(creditHistories.get(creditHistories.size() -1 ));
        }
        return paymentEvent;
    }
}
