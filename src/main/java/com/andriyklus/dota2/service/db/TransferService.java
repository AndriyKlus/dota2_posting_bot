package com.andriyklus.dota2.service.db;

import com.andriyklus.dota2.domain.Transfer;
import com.andriyklus.dota2.repository.TransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final TransferRepository transferRepository;

    public Optional<Transfer> getLastTransfer() {
        return transferRepository.findAll().stream().findAny();
    }

    public Transfer saveLastTransfer(Transfer transfer) {
        transferRepository.deleteAll();
        return transferRepository.save(transfer);
    }

}
