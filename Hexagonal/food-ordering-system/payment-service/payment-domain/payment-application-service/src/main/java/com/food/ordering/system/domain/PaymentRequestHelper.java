package com.food.ordering.system.domain;

import com.food.ordering.system.domain.dto.PaymentRequest;
import com.food.ordering.system.domain.exception.PaymentApplicationServiceException;
import com.food.ordering.system.domain.mapper.PaymentDataMapper;
import com.food.ordering.system.domain.ports.output.repository.CreditEntryRepository;
import com.food.ordering.system.domain.ports.output.repository.CreditHistoryRepository;
import com.food.ordering.system.domain.ports.output.repository.PaymentRepository;
import com.food.ordering.system.domain.valueobject.CustomerId;
import com.food.ordering.system.domain.valueobject.PaymentStatus;
import com.food.ordering.system.payment.service.domain.PaymentDomainService;
import com.food.ordering.system.payment.service.domain.entity.CreditEntry;
import com.food.ordering.system.payment.service.domain.entity.CreditHistory;
import com.food.ordering.system.payment.service.domain.entity.Payment;
import com.food.ordering.system.payment.service.domain.event.PaymentEvent;
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
    public PaymentRequestHelper(PaymentDomainService paymentDomainService,
                                PaymentDataMapper paymentDataMapper,
                                PaymentRepository paymentRepository,
                                CreditEntryRepository creditEntryRepository,
                                CreditHistoryRepository creditHistoryRepository) {
        this.paymentDomainService = paymentDomainService;
        this.paymentDataMapper = paymentDataMapper;
        this.paymentRepository = paymentRepository;
        this.creditEntryRepository = creditEntryRepository;
        this.creditHistoryRepository = creditHistoryRepository;
    }

    @Transactional
    public PaymentEvent persistPayment(PaymentRequest paymentRequest) {
        log.info("Received payment complete event for order id: {}", paymentRequest.getOrderId());
        Payment payment = paymentDataMapper.paymentRequestModelToPayment(paymentRequest);
        CreditEntry creditEntry = getCreditEntry(payment.getCustomerId());
        List<CreditHistory> creditHistories = getCreditHistory(payment.getCustomerId());
        List<String> failureMessages = new ArrayList<>();
        PaymentEvent paymentEvent = paymentDomainService.validateAndInitiatePayment(payment, creditEntry, creditHistories, failureMessages);
        paymentRepository.save(payment);
        persistDBObject(failureMessages, creditEntry, creditHistories, paymentEvent);
        return paymentEvent;
    }

    private CreditEntry getCreditEntry(CustomerId customerId) {
        Optional<CreditEntry> creditEntry = creditEntryRepository.findByCustomerId(customerId);
        if (creditEntry.isEmpty()) {
            log.error("Could not find credit entry for customer: {}", customerId.getValue());
            throw new PaymentApplicationServiceException("Could not find credit entry for customer: " + customerId.getValue());
        }
        return creditEntry.get();
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
    public PaymentEvent persistCancelPayment(PaymentRequest paymentRequest) {
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
        return paymentEvent;
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
