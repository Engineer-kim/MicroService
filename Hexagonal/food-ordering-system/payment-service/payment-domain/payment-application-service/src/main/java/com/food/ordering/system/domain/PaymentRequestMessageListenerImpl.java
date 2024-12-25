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
    public PaymentRequestMessageListenerImpl(PaymentRequestHelper paymentRequestHelper) {
        this.paymentRequestHelper = paymentRequestHelper;

    }

    @Override
    public void completePayment(PaymentRequest paymentRequest) {
       PaymentEvent payment = paymentRequestHelper.persistPayment(paymentRequest);
       fireEvent(payment);
    }


    @Override
    public void cancelPayment(PaymentRequest paymentRequest) {
        PaymentEvent payment = paymentRequestHelper.persistCancelPayment(paymentRequest);
        fireEvent(payment);
    }

    private void fireEvent(PaymentEvent paymentEvent) {
        paymentEvent.fire();
//        if(paymentEvent instanceof PaymentCompletedEvent) {
//            paymentCompletedMessagePublisher.publish((PaymentCompletedEvent) paymentEvent);
//        } else if(paymentEvent instanceof PaymentCancelledEvent) {
//            paymentCancelledMessagePublisher.publish((PaymentCancelledEvent) paymentEvent);
//        } else if(paymentEvent instanceof PaymentFailedEvent) {
//            paymentFailedMessagePublisher.publish((PaymentFailedEvent) paymentEvent);
//        }
    }
}

