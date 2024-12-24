package com.food.ordering.system.domain;

import com.food.ordering.system.domain.dto.PaymentRequest;
import com.food.ordering.system.domain.ports.input.message.listener.PaymentRequestMessageListener;
import com.food.ordering.system.domain.ports.output.message.publisher.PaymentCancelledMessagePublisher;
import com.food.ordering.system.domain.ports.output.message.publisher.PaymentCompletedMessagePublisher;
import com.food.ordering.system.domain.ports.output.message.publisher.PaymentFailedMessagePublisher;
import com.food.ordering.system.payment.service.domain.entity.Payment;
import com.food.ordering.system.payment.service.domain.event.PaymentCancelledEvent;
import com.food.ordering.system.payment.service.domain.event.PaymentCompletedEvent;
import com.food.ordering.system.payment.service.domain.event.PaymentEvent;
import com.food.ordering.system.payment.service.domain.event.PaymentFailedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PaymentRequestMessageListenerImpl implements PaymentRequestMessageListener {

    private final PaymentRequestHelper paymentRequestHelper;
    private final PaymentCompletedMessagePublisher paymentCompletedMessagePublisher;
    private final PaymentCancelledMessagePublisher paymentCancelledMessagePublisher;
    private final PaymentFailedMessagePublisher paymentFailedMessagePublisher;

    public PaymentRequestMessageListenerImpl(PaymentRequestHelper paymentRequestHelper, PaymentCompletedMessagePublisher paymentCompletedMessagePublisher, PaymentCancelledMessagePublisher paymentCancelledMessagePublisher, PaymentFailedMessagePublisher paymentFailedMessagePublisher) {
        this.paymentRequestHelper = paymentRequestHelper;
        this.paymentCompletedMessagePublisher = paymentCompletedMessagePublisher;
        this.paymentCancelledMessagePublisher = paymentCancelledMessagePublisher;
        this.paymentFailedMessagePublisher = paymentFailedMessagePublisher;
    }

    @Override
    public void completeRequest(PaymentRequest paymentRequest) {
       PaymentEvent payment = paymentRequestHelper.persistPayment(paymentRequest);
       fireEvent(payment);
    }


    @Override
    public void cancelPayment(PaymentRequest paymentRequest) {
        PaymentEvent payment = paymentRequestHelper.persistCancelPayment(paymentRequest);
        fireEvent(payment);
    }

    private void fireEvent(PaymentEvent paymentEvent) {
        if(paymentEvent instanceof PaymentCompletedEvent) {
            paymentCompletedMessagePublisher.publish((PaymentCompletedEvent) paymentEvent);
        }
        else if(paymentEvent instanceof PaymentCancelledEvent) {
            paymentCancelledMessagePublisher.publish((PaymentCancelledEvent) paymentEvent);
        } else if(paymentEvent instanceof PaymentFailedEvent) {
            paymentFailedMessagePublisher.publish((PaymentFailedEvent) paymentEvent);
        }
    }
}

