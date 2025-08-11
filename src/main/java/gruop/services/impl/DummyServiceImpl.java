package gruop.services.impl;

import gruop.models.Dummy;
import gruop.repositories.DummyRespository;
import gruop.services.DummyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DummyServiceImpl implements DummyService {

    @Autowired
    private DummyRespository dummyRepository;
    @Override
    public Dummy getDummy(Long id) {
        return new Dummy();
    }

    @Override
    public List<Dummy> getDummyList() {
        return null;
    }

    @Override
    public Dummy create(Dummy dummy) {
        return null;
    }

    @Override
    public Dummy update(Dummy dummy) {
        return null;
    }

    @Override
    public void deleteDummy(Dummy dummy) {

    }
}
