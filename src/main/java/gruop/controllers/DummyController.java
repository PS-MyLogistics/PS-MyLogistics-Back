package gruop.controllers;

import gruop.dtos.DummyDto;
import gruop.models.Dummy;
import gruop.services.DummyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dummy")
public class DummyController {

    @Autowired
    private DummyService dummyService;

    @GetMapping("")
   public ResponseEntity<DummyDto> getDummyList (){
        List<Dummy> dummyList= dummyService.getDummyList();
        return null;
    }

    @GetMapping("/{id}")
    public ResponseEntity<DummyDto> getDummy(@PathVariable Long id ){
        Dummy dummy= dummyService.getDummy(id);
        return null;
    }

    @PostMapping("")
    public ResponseEntity<DummyDto> createDummy(){
     Dummy dummy = dummyService.create(null);
        return null;
    }

   @PutMapping("")
    public  ResponseEntity<DummyDto> updateDummy(DummyDto dummyDto){
        Dummy dummy = dummyService.update(null);
        return null;
   }

   @DeleteMapping("")
    public  ResponseEntity<DummyDto> deleteDummy(DummyDto dummyDto){
      dummyService.deleteDummy(null);
        return null;
   }
}
