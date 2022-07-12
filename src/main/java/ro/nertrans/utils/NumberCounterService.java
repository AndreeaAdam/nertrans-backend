package ro.nertrans.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class NumberCounterService {

    @Autowired
    private NumberCounterRepository numberCounterRepository;

    private static NumberCounter single_instance = null;

    public static NumberCounter getInstance()
    {
        if (single_instance == null)
            single_instance = new NumberCounter(1);

        return single_instance;
    }

    public long getNextUser() {

        long number;
        Optional<NumberCounter> last = numberCounterRepository.findTopByOrderByIdDesc();
        if (numberCounterRepository.findTopByOrderByIdDesc().isPresent()) {
            long lastNum = last.get().getSeqUser();
            last.get().setSeqUser(lastNum + 1);
            numberCounterRepository.save(last.get());
            number = last.get().getSeqUser();
        } else{
        numberCounterRepository.save(getInstance());
        number = 1;
        }
        return number;
    }

    public long getNextPartner() {

        long number;
        Optional<NumberCounter> last = numberCounterRepository.findTopByOrderByIdDesc();
        if (numberCounterRepository.findTopByOrderByIdDesc().isPresent()) {
            long lastNum = last.get().getSeqPartner();
            last.get().setSeqPartner(lastNum + 1);
            numberCounterRepository.save(last.get());
            number = last.get().getSeqPartner();
        } else{
            numberCounterRepository.save(getInstance());
            number = 1;
        }
        return number;
    }
}
