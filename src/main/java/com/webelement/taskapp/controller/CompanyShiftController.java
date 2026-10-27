package com.webelement.taskapp.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webelement.taskapp.dto.CompanyShiftDto;
import com.webelement.taskapp.service.CompanyShiftService;

@RestController
@RequestMapping("/admin/companyShift")
public class CompanyShiftController {

    @Autowired
    private CompanyShiftService service;

    @PostMapping("/add")
    public Map<String, Object> add(@RequestBody CompanyShiftDto dto) { return service.add(dto); }

    @PutMapping("/update")
    public Map<String, Object> update(@RequestBody CompanyShiftDto dto) { return service.update(dto); }

    @DeleteMapping("/delete/{shiftId}")
    public Map<String, Object> delete(@PathVariable Integer shiftId) { return service.delete(shiftId); }

    @GetMapping("/viewById/{shiftId}")
    public Map<String, Object> viewById(@PathVariable Integer shiftId) { return service.viewById(shiftId); }

    @GetMapping("/viewList/{companyId}")
    public Map<String, Object> viewList(@PathVariable Integer companyId) { return service.viewList(companyId); }
}
