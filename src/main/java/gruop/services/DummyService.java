package gruop.services;

import gruop.models.Dummy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface DummyService {

    Dummy getDummy(Long id);

    List<Dummy> getDummyList();

    Dummy create(Dummy dummy);

    Dummy update(Dummy dummy);

    void deleteDummy(Dummy dummy);


}
