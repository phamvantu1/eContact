package com.ec.contract.service;

import com.ec.contract.model.dto.response.DashBoardStatisticDTO;
import com.ec.contract.repository.ContractRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashBoardService {

    private final ContractRepository contractRepository;

    public DashBoardStatisticDTO getMyProcessDashboard(){
        try{

            return null;
        }catch (Exception e){
            log.error("Error in getMyProcessDashboard: ", e);
            return null;
        }
    }
}
