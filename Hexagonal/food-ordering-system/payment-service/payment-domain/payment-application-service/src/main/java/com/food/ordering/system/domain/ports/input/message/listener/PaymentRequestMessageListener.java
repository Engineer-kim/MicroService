package com.food.ordering.system.domain.ports.input.message.listener;

import com.food.ordering.system.domain.dto.PaymentRequest;

public interface PaymentRequestMessageListener {

    void completeRequest(PaymentRequest paymentRequest);

    void cancelPayment(PaymentRequest paymentRequest);
}
