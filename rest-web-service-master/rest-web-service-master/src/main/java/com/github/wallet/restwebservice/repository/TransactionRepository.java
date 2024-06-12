package com.github.wallet.restwebservice.repository;

import com.github.wallet.restwebservice.advice.WalletException;
import com.github.wallet.restwebservice.models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional(rollbackOn = WalletException.class)
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByWalletId(long walletId);

    /*Transaction findByGlobalId(String globalId);*/

    List<Transaction> findByWalletUserIdAndLectureIdsAndTypeId(long userId, long lectureId, int typeId);

    List<Transaction> findByWalletUserIdAndGroupIdAndTypeId(long userId, long groupId, int typeId);
    List<Transaction> findByWalletUserId(long userId);

    List<Transaction> findByTeacherId(long teacherId);
}
