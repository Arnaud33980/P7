package com.arnaud.back.blibliotheque.services.impl;

import com.arnaud.back.blibliotheque.exception.EntityNotFoundException;
import com.arnaud.back.blibliotheque.exception.ErrorCode;
import com.arnaud.back.blibliotheque.model.*;
import com.arnaud.back.blibliotheque.repository.*;
import com.arnaud.back.blibliotheque.services.BorrowingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Service
@Slf4j
public class BorrowingServicesmpl implements BorrowingService {

    final BorrowingRepository borrowingRepository;
    final AccountRepository accountRepository;
    final BookRepository bookRepository;
    private final JavaMailSenderImpl javaMailSenderImpl;
    private final ExemplaryRepository exemplaryRepository;
    final LoanRepository loanRepository;

    @Autowired
    public BorrowingServicesmpl(BorrowingRepository borrowingRepository, AccountRepository accountRepository, BookRepository bookRepository, JavaMailSenderImpl javaMailSenderImpl, ExemplaryRepository exemplaryRepository, LoanRepository loanRepository) {
        this.borrowingRepository = borrowingRepository;
        this.accountRepository = accountRepository;
        this.bookRepository = bookRepository;
        this.javaMailSenderImpl = javaMailSenderImpl;
        this.exemplaryRepository = exemplaryRepository;
        this.loanRepository = loanRepository;
    }

    @Override
    //TODO méhode a tester
    public Borrowing save(Borrowing borrowing, long exemplaryId, Integer accountid, Integer bookid) {

        // recupére l'utilisateur courrant
       Account account = accountRepository.findById(accountid).orElse(null);
        borrowing.setAccount(account);
        // récupére l'exemplaire courrant
        Exemplary exemplary = exemplaryRepository.findById(exemplaryId).orElse(null);
        borrowing.setExemplaryId(exemplary);
        Book book = bookRepository.findById(bookid).orElse(null);
        borrowing.setBook(book);
        borrowing.setBookingDate(LocalDateTime.now());
        borrowing.setBookingDateEnd(LocalDateTime.now().plusDays(2));
        int totalnbr = borrowingRepository.countBorrowingByBookBookId(bookid);
        borrowing.setTotal(totalnbr+1);
        int queue  =  borrowing.setCmpt(borrowingRepository.countBorrowingByBookBookId(bookid)+1);

        //vérifie que la liste ne soit pas égale au nombre max d'exemplaire (n'est pas supérieure à 2x le nombre d'exemplaire)
        //sinon lévé exception
        assert exemplary != null;
        if(queue>exemplary.getMaxExemplaryNumber()){
            log.error("max atteint");
            throw new EntityNotFoundException("le nombre maximum de réservation pour ce livre est atteint",ErrorCode.EXEMPLARY_LIMIT_REACHED);
        }


       Optional<Loan> loans = loanRepository.findById(Math.toIntExact(exemplaryId));
        /**
         * Il n’est pas possible pour un usager de réserver un ouvrage qu’il a déjà en cours d’emprunt
         */

      if(loans.isPresent()){
          throw new EntityNotFoundException("vous avez dèjà emprinté ce livre il est impossible de le réservé",ErrorCode.BORROWING_NOT_VALID);

      }else
      {
          javaMailSenderImpl.sendEmail(account.getMail(),"votre réservation au nom de"+ " " +account.getFristName()+ " en date du " +borrowing.getBookingDate()
                          +" a été crée avec succès"
                  ,"vous avez jusque au"+ borrowing.getBookingDateEnd()+" pour venir chercher vôtre réservation,sinon elle sera automatiquement supprimée");
          return borrowingRepository.save(borrowing);
      }


    }


    @Override
    public void deleteBorrowingById(Borrowing borrowing, Integer id, Integer accountid, long exemplaryId,int bookid) {

        // recupération de l'id du compte
        Account account = accountRepository.findById(accountid).orElseThrow(()->new EntityNotFoundException("aucun utilisateur trouvé",ErrorCode.USER_NOT_FOUND));
        borrowing.setAccount(account);
        //recupération de l'id de l exemplaire
        Exemplary exemplary = exemplaryRepository.findById(exemplaryId).orElseThrow(()->new EntityNotFoundException("aucun exemplaire trouvé", ErrorCode.EXEMPLARY_NOT_FOUND));
        borrowing.setExemplaryId(exemplary);

        borrowingRepository.updateCmptBorrowing(bookid );
            javaMailSenderImpl.sendEmail(account.getMail(),"votre réservation au nom de"+ " " +account.getFristName()+ " en date du " +borrowing.getBookingDate()
                            +" a été crée avec succès"
                    ,"vôtre reservation a été supprimé ");
            borrowingRepository.deleteById(id);

    }

    @Override
    public List<Borrowing> findBorrrowingOutOfTime() {

        LocalDateTime dateDuJour = LocalDateTime.now();
        List<Borrowing> borrowings = borrowingRepository.findAllByBookingDateEndLessThan(dateDuJour);

       return borrowings;
    }

    @Override
    public void  isOutOfTime() {
        borrowingRepository.isOutOfTime();
    }


    @Override
    public List<Borrowing> findAllByAccountId(Integer id) {
        return borrowingRepository.findAllByAccountId(id);
    }

    @Override
    public List<Borrowing> findall() {
        return borrowingRepository.findAll();
    }

    @Override
    public List<Borrowing> findAllByBookAvailable(boolean available) {
        return borrowingRepository.findAllByBookAvailable(available);
    }

    @Override
    public void deleteBorrowingByOutOfTimeTrue() {
        borrowingRepository.deleteBorrowingByOutOfTimeTrue();
    }


}

