package com.nikonenko.passengerservice.services;

import com.nikonenko.passengerservice.dto.PassengerRequest;
import com.nikonenko.passengerservice.dto.PassengerResponse;
import com.nikonenko.passengerservice.dto.RatingPassengerRequest;
import com.nikonenko.passengerservice.exceptions.PassengerNotFoundException;
import com.nikonenko.passengerservice.exceptions.PhoneAlreadyExistsException;
import com.nikonenko.passengerservice.exceptions.UsernameAlreadyExistsException;
import com.nikonenko.passengerservice.exceptions.WrongPageableParameterException;
import com.nikonenko.passengerservice.models.Passenger;
import com.nikonenko.passengerservice.models.RatingPassenger;
import com.nikonenko.passengerservice.repositories.PassengerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class PassengerServiceImpl implements PassengerService{

    private final PassengerRepository passengerRepository;
    private final ModelMapper modelMapper;

    @Override
    public Page<PassengerResponse> getAllPassengers(int pageNumber, int pageSize, String sortField) {
        if (pageNumber < 0 || pageSize < 1) {
            throw new WrongPageableParameterException();
        }
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortField));
        Page<Passenger> page = passengerRepository.findAll(pageable);
        return modelMapper.map(page, new TypeToken<Page<PassengerResponse>>() {}.getType());
    }

    @Override
    public PassengerResponse getPassengerById(Long id) {
        return modelMapper.map(getPassenger(id), PassengerResponse.class);
    }

    @Override
    public PassengerResponse createPassenger(PassengerRequest passengerRequest) {
        checkPassengerExists(passengerRequest);
        Passenger passenger = modelMapper.map(passengerRequest, Passenger.class);
        Passenger savedPassenger = passengerRepository.save(passenger);
        return modelMapper.map(savedPassenger, PassengerResponse.class);
    }

    @Override
    public PassengerResponse editPassenger(Long id, PassengerRequest passengerRequest) {
        checkPassengerExists(passengerRequest);
        Passenger editingPassenger = getPassenger(id);
        modelMapper.map(passengerRequest, editingPassenger);
        passengerRepository.save(editingPassenger);
        return modelMapper.map(editingPassenger, PassengerResponse.class);
    }

    @Override
    public void deletePassenger(Long id) {
        passengerRepository.delete(getPassenger(id));
    }

    @Override
    public PassengerResponse createReview(Long id, RatingPassengerRequest ratingRequest) {
        Passenger passenger = getPassenger(id);

        RatingPassenger addingRating = modelMapper.map(ratingRequest, RatingPassenger.class);
        addingRating.setPassenger(passenger);

        Set<RatingPassenger> modifiedRatingSet = passenger.getRatingSet();
        modifiedRatingSet.add(addingRating);

        passenger.setRatingSet(modifiedRatingSet);
        return modelMapper.map(passengerRepository.save(passenger), PassengerResponse.class);
    }

    public Passenger getPassenger(Long id) {
        Optional<Passenger> optionalPassenger = passengerRepository.findById(id);
        if (optionalPassenger.isEmpty()) {
            throw new PassengerNotFoundException();
        }
        return optionalPassenger.get();
    }

    public void checkPassengerExists(PassengerRequest passengerRequest) {
        if (passengerRepository.existsByPhone(passengerRequest.getPhone())) {
            throw new PhoneAlreadyExistsException();
        }
        if (passengerRepository.existsByUsername(passengerRequest.getUsername())) {
            throw new UsernameAlreadyExistsException();
        }
    }
}