package com.github.wallet.restwebservice.service;

import com.github.wallet.restwebservice.advice.WalletException;
import com.github.wallet.restwebservice.converter.TransactionDtoConverter;
import com.github.wallet.restwebservice.models.*;
import com.github.wallet.restwebservice.proxy.CoursProxy;
import com.github.wallet.restwebservice.proxy.UserProxy;
import com.github.wallet.restwebservice.repository.TransactionRepository;
import com.github.wallet.restwebservice.repository.TypeRepository;
import com.github.wallet.restwebservice.repository.WalletRepository;
import com.github.wallet.restwebservice.service.contracts.ITransactionService;
import com.github.wallet.restwebservice.service.models.PaymentIntentResponse;
import com.github.wallet.restwebservice.service.models.TransactionDTO;
import com.github.wallet.restwebservice.service.models.TransactionDTODisplay;
import com.stripe.param.PaymentIntentConfirmParams;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;


@Service
@Slf4j
@PropertySource("classpath:application.properties")
public class TransactionService implements ITransactionService {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private WalletService walletService;
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private TypeRepository typeRepository;
    @Autowired
    CoursProxy coursProxy;
    @Autowired
    UserProxy userProxy;
    private TransactionDtoConverter transactionConverter = new TransactionDtoConverter();

    static {
        Stripe.apiKey = "sk_test_51POoWEAugimTheFEO50QMURfVtYYgMN81pUt9gaNv554XmBrFI1v7S0vA9nrn2MKH3iuPVWqhSi4N6pGLWfvyYxS00DKIWDCtk"; // Replace with your actual Stripe secret key
    }

    @Transactional(rollbackFor = WalletException.class)
    @Override
    public List<TransactionDTO> getTransactionsByWalletId(@NotNull long walletId) throws WalletException {
        try {
            return transactionRepository.findByWalletId(walletId).stream()
                    .map(w -> transactionConverter.convert(w))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new WalletException(400, "WalletId does not exist - getTransactionsByWalletId method");
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE, rollbackFor = WalletException.class)
    @Override
    public TransactionDTO createTransactionAndChangeBalance(int typeId, double amount, long walletId, List<Long> lectureIds, Long teacherId, Long groupId) throws WalletException {
        try {
            if (typeId == 1) {
                walletService.deleteWalletAmount(walletId, amount);
            } else if (typeId == 2) {
                walletService.addWalletAmount(walletId, amount);
            }
            Optional<Wallet> addedWallet = walletRepository.findById(walletId);

            if (addedWallet.isPresent()) {
                // Ensure null values are properly handled
                Transaction addTransaction = new Transaction(typeRepository.getOne(typeId), amount, addedWallet.get(),
                        lectureIds != null ? lectureIds : Collections.emptyList(),
                        teacherId, groupId);
                Transaction createdTransaction = transactionRepository.save(addTransaction);
                if (createdTransaction != null) {
                    return transactionConverter.convert(createdTransaction);
                } else {
                    throw new WalletException(400, "Transaction couldn't be created. - createTransactionAndChangeBalance method");
                }
            } else {
                throw new WalletException(400, "Transaction couldn't be created. No suitable wallet exists - createTransactionAndChangeBalance method");
            }
        } catch (NumberFormatException e) {
            throw new WalletException(400, "Format Exception");
        }
    }



    @Override
    public TransactionDTO getDebitTransactionByUserIdAndLectureId(long userId, long lectureId) throws WalletException {
        try {
            List<Transaction> debitTransactions = transactionRepository.findByWalletUserIdAndLectureIdsAndTypeId(userId, lectureId, 1);
            if (!debitTransactions.isEmpty()) {
                return transactionConverter.convert(debitTransactions.get(0));
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new WalletException(400, "Error retrieving debit transactions by user ID and lecture ID");
        }
    }
    @Override
    public List<TransactionDTO> getDebitTransactionsByUserIdAndGroupId(long userId, long groupId) throws WalletException {
        try {
            List<Transaction> debitTransactions = transactionRepository.findByWalletUserIdAndGroupIdAndTypeId(userId, groupId, 1);
            return debitTransactions.stream()
                    .map(transactionConverter::convert)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new WalletException(400, "Error retrieving debit transactions by user ID and group ID");
        }
    }

    @Override
    public List<TransactionDTODisplay> getTransactionsByUserId(long userId,String authorizationHeader) throws WalletException {
        List<TransactionDTODisplay> transactionDTODisplays=new ArrayList<>();
        try {
            List<Transaction> transactions= transactionRepository.findByWalletUserId(userId);
            transactions.stream()
                    .map(transaction ->  {
                        String type="student";
                        TransactionDTODisplay transactionDTODisplay = new TransactionDTODisplay();
                        transactionDTODisplay.setAmount(transaction.getAmount());

                        if (type == "student") {
                            try{
                                User user = userProxy.getTeacher(transaction.getTeacherId(), "topayment", authorizationHeader);
                                transactionDTODisplay.setUser(user);
                            }
                            catch (Exception e){
                                transactionDTODisplay.setUser(null);
                            }

                        } else {
                            try {
                                log.info("userid" + transaction.getWallet().getUserId());
                                User user = userProxy.getStudent(transaction.getWallet().getUserId(), "topayment", authorizationHeader);
                                transactionDTODisplay.setUser(user);
                            }
                            catch (Exception e){
                                transactionDTODisplay.setUser(null);
                            }
                        }


                        transactionDTODisplay.setTypeId(transaction.getType().getId());
                        List<Lecture> lectures = new ArrayList<>();
                        transaction.getLectureIds().stream().forEach(lectureId -> {
                            try {
                                Lecture lecture = coursProxy.getLecture(lectureId, "topayment", authorizationHeader);
                                lectures.add(lecture);
                            } finally {

                            }

                        });
                        transactionDTODisplay.setLectures(lectures);
                        try {
                            Groupe groupe = coursProxy.getGroup(transaction.getGroupId(), "topayment", authorizationHeader);
                            transactionDTODisplay.setGroup(groupe);
                        }catch (Exception e){
                            transactionDTODisplay.setGroup(null);
                        }

                        transactionDTODisplay.setLastUpdated(transaction.getLastUpdated());
                        transactionDTODisplays.add(transactionDTODisplay);
                        return transaction;
                    })
                    .collect(Collectors.toList());
            return transactionDTODisplays;
        } catch (Exception e) {
            throw new WalletException(400, "Error retrieving transactions by user ID");
        }
    }

    @Override
    public List<TransactionDTO> getTransactionsByTeacherId(long teacherId) throws WalletException {
        try {
            return transactionRepository.findByTeacherId(teacherId)
                    .stream()
                    .map(transactionConverter::convert)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new WalletException(400, "Error retrieving transactions by teacher ID");
        }
    }

    public List<TransactionDTODisplay> getDebitTransactionsByTeacherId(long teacherId,String authorizationHeader) throws WalletException {
        List<TransactionDTODisplay> transactionDTODisplays=new ArrayList<>();
        try {
            List<Transaction> transactions = transactionRepository.findByTeacherId(teacherId)
                    .stream()
                    .filter(transaction -> transaction.getType().getId() == 1) // Filter for debit transactions (typeId=1)
                    .collect(Collectors.toList());
             transactions.stream()
                    .map(transaction ->  {
                        String type="teacher";
                        TransactionDTODisplay transactionDTODisplay = new TransactionDTODisplay();
                        transactionDTODisplay.setAmount(transaction.getAmount());

                        if (type == "student") {
                            try{
                                User user = userProxy.getTeacher(transaction.getTeacherId(), "topayment", authorizationHeader);
                                transactionDTODisplay.setUser(user);
                            }
                            catch (Exception e){
                                transactionDTODisplay.setUser(null);
                            }

                        } else {
                            try {
                                log.info("userid" + transaction.getWallet().getUserId());
                                User user = userProxy.getStudent(transaction.getWallet().getUserId(), "topayment", authorizationHeader);
                                transactionDTODisplay.setUser(user);
                            }
                            catch (Exception e){
                                transactionDTODisplay.setUser(null);
                            }
                        }


                        transactionDTODisplay.setTypeId(transaction.getType().getId());
                        List<Lecture> lectures = new ArrayList<>();
                        transaction.getLectureIds().stream().forEach(lectureId -> {
                            try {
                                Lecture lecture = coursProxy.getLecture(lectureId, "topayment", authorizationHeader);
                                lectures.add(lecture);
                            } finally {

                            }

                        });
                        transactionDTODisplay.setLectures(lectures);
                        try {
                            Groupe groupe = coursProxy.getGroup(transaction.getGroupId(), "topayment", authorizationHeader);
                            transactionDTODisplay.setGroup(groupe);
                        }catch (Exception e){
                            transactionDTODisplay.setGroup(null);
                        }

                        transactionDTODisplay.setLastUpdated(transaction.getLastUpdated());
                        transactionDTODisplays.add(transactionDTODisplay);
                        return transaction;
                    })
                    .collect(Collectors.toList());

            return transactionDTODisplays;


        } catch (Exception e) {
            throw new WalletException(400, e.toString());
        }
    }
    public PaymentIntentResponse createStripePaymentIntent(double amountInDZD) throws StripeException {
        // Set your secret key: remember to switch to your live secret key in production
        // See your keys here: https://dashboard.stripe.com/apikeys
        Stripe.apiKey = "sk_test_51POoWEAugimTheFEO50QMURfVtYYgMN81pUt9gaNv554XmBrFI1v7S0vA9nrn2MKH3iuPVWqhSi4N6pGLWfvyYxS00DKIWDCtk";

        // Convert amount from DZD to USD
        double exchangeRate = 0.0074; // Example exchange rate, you should get the latest rate from a reliable source
        double amountInUSD = amountInDZD * exchangeRate;

        // Create a PaymentIntent with the order amount and currency
        Map<String, Object> params = new HashMap<>();
        params.put("amount", (int) (amountInUSD * 100)); // Stripe expects the amount in cents
        params.put("currency", "usd"); // Charge in USD

        PaymentIntent paymentIntent = PaymentIntent.create(params);

        PaymentIntentResponse response = new PaymentIntentResponse();
        response.setId(paymentIntent.getId());
        response.setClientSecret(paymentIntent.getClientSecret());

        return response;
    }

    public String confirmStripePaymentIntent(String paymentIntentId, String paymentMethodId) throws StripeException {
        // Set your secret key: remember to switch to your live secret key in production
        // See your keys here: https://dashboard.stripe.com/apikeys
        Stripe.apiKey = "sk_test_51POoWEAugimTheFEO50QMURfVtYYgMN81pUt9gaNv554XmBrFI1v7S0vA9nrn2MKH3iuPVWqhSi4N6pGLWfvyYxS00DKIWDCtk";

        // Retrieve the PaymentIntent and confirm it with the specified payment method
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

        Map<String, Object> params = new HashMap<>();
        params.put("payment_method", paymentMethodId);

        paymentIntent = paymentIntent.confirm(params);

        return paymentIntent.getStatus();
    }
}
