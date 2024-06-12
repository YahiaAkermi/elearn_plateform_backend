package com.github.wallet.restwebservice.controller;

import com.github.wallet.restwebservice.advice.ErrorResponse;
import com.github.wallet.restwebservice.advice.RateLimiterException;
import com.github.wallet.restwebservice.advice.WalletException;
import com.github.wallet.restwebservice.aspect.RateLimit;
import com.github.wallet.restwebservice.proxy.StatisticsProxy;
import com.github.wallet.restwebservice.service.contracts.ITransactionService;
import com.github.wallet.restwebservice.service.contracts.IWalletService;
import com.github.wallet.restwebservice.service.models.ConfirmTransactionRequest;
import com.github.wallet.restwebservice.service.models.PaymentIntentResponse;
import com.github.wallet.restwebservice.service.models.TransactionDTO;
import com.github.wallet.restwebservice.service.models.TransactionDTODisplay;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
@RequestMapping("/api/v1")
public class TransactionController {

    Logger logger = LoggerFactory.getLogger(TransactionController.class);

    @Autowired
    private ITransactionService transactionService;
    @Autowired
    private IWalletService walletService;

    @Autowired
    private Environment env;

    @Autowired
    private StatisticsProxy statisticsProxy;

    @GetMapping("/wallets/{id}/transactions")
    @RateLimit(limit = 50, duration = 60, unit = TimeUnit.SECONDS)
    @ApiOperation(value = "Find all transactions by given walletId", notes = "Returns a collection of transactions by given walletId")
    public ResponseEntity<?> getByWalletId(@PathVariable("id") long id) throws RateLimiterException {

        logger.info("TransactionController getByWalletId method calls for getting all transactions");

        List<TransactionDTO> transactions = transactionService.getTransactionsByWalletId(id);

        if (!transactions.isEmpty()) {
            return ResponseEntity.ok().body(transactions);
        } else {
            throw new WalletException(404, "Transactions not found");
        }
    }

    @PostMapping("/transaction")
    @RateLimit(limit = 50, duration = 60, unit = TimeUnit.SECONDS)
    @ApiOperation(value = "Add transaction and add balance", notes = "Create transaction and add balance")
    public ResponseEntity<?> creditOrDebit(@RequestBody TransactionDTO transaction) throws RateLimiterException {
        logger.info("TransactionController add method is calling");

        int typeId = transaction.getTypeId();
        double amount = transaction.getAmount();
        long walletId = transaction.getWalletId();
        List<Long> lectureIds = transaction.getLectureIds();
        Long teacherId = transaction.getTeacherId();
        Long groupId = transaction.getGroupId();

        // Validation checks
        if (typeId != 1 && typeId != 2) {
            return new ResponseEntity<>(new ErrorResponse("TypeId should be debit or credit", 400), HttpStatus.BAD_REQUEST);
        }
        if (amount < 0) {
            return new ResponseEntity<>(new ErrorResponse("Balance should not be negative", 400), HttpStatus.BAD_REQUEST);
        }
        if (typeId == 1 && walletService.findById(walletId).getBalance() < amount) {
            return new ResponseEntity<>(new ErrorResponse("Insufficient balance", 400), HttpStatus.BAD_REQUEST);
        }

        try {
            if (typeId == 2) {
                // Create PaymentIntent
                PaymentIntentResponse paymentIntentResponse = transactionService.createStripePaymentIntent(amount);
                logger.info("Stripe PaymentIntent created with ID: " + paymentIntentResponse.getId());

                // Return client secret to client
                return new ResponseEntity<>(paymentIntentResponse, HttpStatus.CREATED);
            } else {
                TransactionDTO createdTransaction = transactionService.createTransactionAndChangeBalance(
                        typeId, amount, walletId, lectureIds, teacherId, groupId);
                logger.info("Transaction created: " + createdTransaction.getWalletId());

                // Handle follow-up credit transaction to teacher
                if (teacherId != null) {
                    TransactionDTO teacherTransaction = new TransactionDTO();
                    teacherTransaction.setTypeId(2);  // Credit type
                    teacherTransaction.setAmount(amount);
                    teacherTransaction.setWalletId(walletService.findByUserId(teacherId).getId());
                    teacherTransaction.setLectureIds(lectureIds);
                    teacherTransaction.setTeacherId(teacherId);
                    teacherTransaction.setGroupId(groupId);

                    transactionService.createTransactionAndChangeBalance(
                            teacherTransaction.getTypeId(), teacherTransaction.getAmount(),
                            teacherTransaction.getWalletId(), teacherTransaction.getLectureIds(),
                            teacherTransaction.getTeacherId(), teacherTransaction.getGroupId());

                for (Long lecture:teacherTransaction.getLectureIds())
                {
                    statisticsProxy.addPayment(walletId,lecture);
                }
                }
                return new ResponseEntity<>(HttpStatus.CREATED);
            }
        } catch (WalletException exc) {
            logger.error("TransactionController creditOrDebit method has an error", exc);
            return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
        } catch (StripeException exc) {
            logger.error("StripeException occurred during payment processing", exc);
            return new ResponseEntity<>(new ErrorResponse("Stripe payment processing failed", 500), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/users/{userId}/lectures/{lectureId}/transactions")
    @RateLimit(limit = 50, duration = 60, unit = TimeUnit.SECONDS)
    @ApiOperation(value = "Find a debit transaction by given user and lectureId", notes = "Returns a debit transaction by given user and lectureId")
    public ResponseEntity<?> getByUserIdAndLectureId(@PathVariable("userId") long userId, @PathVariable("lectureId") long lectureId) throws RateLimiterException {
        logger.info("TransactionController getByUserIdAndLectureId method calls for getting debit transactions");

        TransactionDTO transaction = transactionService.getDebitTransactionByUserIdAndLectureId(userId, lectureId);
        if (transaction != null) {
            return ResponseEntity.ok().body(transaction);
        } else {
            throw new WalletException(404, "Debit transaction not found for the specified user and lecture");
        }
    }

    @GetMapping("/users/{userId}/groups/{groupId}/transactions")
    @RateLimit(limit = 50, duration = 60, unit = TimeUnit.SECONDS)
    @ApiOperation(value = "Find debit transactions by given user and groupId", notes = "Returns debit transactions by given user and groupId")
    public ResponseEntity<?> getByUserIdAndGroupId(@PathVariable("userId") long userId, @PathVariable("groupId") long groupId) throws RateLimiterException {
        logger.info("TransactionController getByUserIdAndGroupId method calls for getting debit transactions");

        List<TransactionDTO> transactions = transactionService.getDebitTransactionsByUserIdAndGroupId(userId, groupId);
        if (!transactions.isEmpty()) {
            return ResponseEntity.ok().body(transactions);
        } else {
            throw new WalletException(404, "Debit transactions not found for the specified user and group");
        }
    }

    @GetMapping("/users/{userId}/transactions")
    @RateLimit(limit = 50, duration = 60, unit = TimeUnit.SECONDS)
    @ApiOperation(value = "Find all transactions by given user ID", notes = "Returns a collection of transactions by given user ID")
    public ResponseEntity<?> getByUserId(@PathVariable("userId") long userId,@RequestHeader("Authorization") String authorizationHeader) throws RateLimiterException {
        logger.info("TransactionController getByUserId method calls for getting transactions");
        List<TransactionDTODisplay> transactions = transactionService.getTransactionsByUserId(userId,authorizationHeader);
        if (!transactions.isEmpty()) {
            return ResponseEntity.ok().body(transactions);
        } else {
            throw new WalletException(404, "Transactions not found for the specified user");
        }
    }
    @GetMapping("/teachers/{teacherId}/transactions")
    @RateLimit(limit = 50, duration = 60, unit = TimeUnit.SECONDS)
    @ApiOperation(value = "Find all debit transactions by given teacher ID", notes = "Returns a collection of debit transactions by given teacher ID")
    public ResponseEntity<?> getDebitTransactionsByTeacherId(@PathVariable("teacherId") long teacherId,@RequestHeader("Authorization") String authorizationHeader) throws RateLimiterException {
        logger.info("TransactionController getDebitTransactionsByTeacherId method calls for getting debit transactions with teacherId");

        List<TransactionDTODisplay> transactions = transactionService.getDebitTransactionsByTeacherId(teacherId,authorizationHeader);
        if (!transactions.isEmpty()) {
            return ResponseEntity.ok().body(transactions);
        } else {
            throw new WalletException(404, "Debit transactions not found for the specified teacher");
        }
    }


    @PostMapping("/confirm")
    @ApiOperation(value = "Confirm transaction", notes = "Confirm transaction using payment intent and payment method IDs")
    public ResponseEntity<?> confirmTransaction(@RequestBody ConfirmTransactionRequest confirmRequest) {
        try {
            // Validate the PaymentIntent status without confirming it again
            PaymentIntent paymentIntent = PaymentIntent.retrieve(confirmRequest.getPaymentIntentId());
            if ("succeeded".equals(paymentIntent.getStatus())) {
                // Create transaction after successful payment
                TransactionDTO createdTransaction = transactionService.createTransactionAndChangeBalance(
                        confirmRequest.getTypeId(), confirmRequest.getAmount(), confirmRequest.getWalletId(),
                        null, null, null);
                return new ResponseEntity<>(createdTransaction, HttpStatus.CREATED);
            } else {
                return new ResponseEntity<>(new ErrorResponse("Payment failed", 400), HttpStatus.BAD_REQUEST);
            }
        } catch (StripeException exc) {
            logger.error("StripeException occurred during payment processing", exc);
            return new ResponseEntity<>(new ErrorResponse("Stripe payment processing failed", 500), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
