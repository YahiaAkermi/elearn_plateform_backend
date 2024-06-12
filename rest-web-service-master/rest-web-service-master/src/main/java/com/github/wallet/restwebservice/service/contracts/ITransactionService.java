package com.github.wallet.restwebservice.service.contracts;

import com.github.wallet.restwebservice.advice.WalletException;
import com.github.wallet.restwebservice.service.models.PaymentIntentResponse;
import com.github.wallet.restwebservice.service.models.TransactionDTO;
import com.github.wallet.restwebservice.service.models.TransactionDTODisplay;
import com.stripe.exception.StripeException;

import javax.validation.constraints.NotNull;
import java.util.List;

public interface ITransactionService {

    List<TransactionDTO> getTransactionsByWalletId(@NotNull long walletId) throws WalletException;
    TransactionDTO createTransactionAndChangeBalance(int typeId, double amount, long userId, List<Long> lectureIds, Long teacherId, Long groupId) throws WalletException;
    TransactionDTO getDebitTransactionByUserIdAndLectureId(long userId, long lectureId) throws WalletException;

    List<TransactionDTO> getDebitTransactionsByUserIdAndGroupId(long userId, long groupId) throws WalletException;

    List<TransactionDTODisplay> getTransactionsByUserId(long userId,String authorization) throws WalletException;
    List<TransactionDTODisplay> getDebitTransactionsByTeacherId(long teacherId, String authorization) throws WalletException;
    List<TransactionDTO> getTransactionsByTeacherId(long teacherId) throws WalletException;

    PaymentIntentResponse createStripePaymentIntent(double amount) throws WalletException, StripeException;
    String confirmStripePaymentIntent(String paymentIntentId, String paymentMethodId) throws StripeException;
}
