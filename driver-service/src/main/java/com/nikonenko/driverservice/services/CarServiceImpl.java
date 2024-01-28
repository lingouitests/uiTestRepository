package com.nikonenko.driverservice.services;

import com.nikonenko.driverservice.dto.CarRequest;
import com.nikonenko.driverservice.dto.CarResponse;
import com.nikonenko.driverservice.dto.DriverResponse;
import com.nikonenko.driverservice.exceptions.CarNotFoundException;
import com.nikonenko.driverservice.exceptions.PhoneAlreadyExistsException;
import com.nikonenko.driverservice.exceptions.WrongPageableParameterException;
import com.nikonenko.driverservice.models.Car;
import com.nikonenko.driverservice.repositories.CarRepository;
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


@Service
@RequiredArgsConstructor
@Slf4j
public class CarServiceImpl implements CarService{

    private final CarRepository carRepository;

    private final ModelMapper modelMapper;

    @Override
    public Page<CarResponse> getAllCars(int pageNumber, int pageSize, String sortField) {
        if (pageNumber < 0 || pageSize < 1) {
            throw new WrongPageableParameterException();
        }
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortField));
        Page<Car> page = carRepository.findAll(pageable);
        return modelMapper.map(page, new TypeToken<Page<DriverResponse>>() {}.getType());
    }

    @Override
    public CarResponse createCar(CarRequest carRequest) {
        checkCarExists(carRequest);
        Car car = modelMapper.map(carRequest, Car.class);
        Car savedCar = carRepository.save(car);
        return modelMapper.map(savedCar, CarResponse.class);
    }

    @Override
    public CarResponse getCarById(Long id) {
        return modelMapper.map(getCar(id), CarResponse.class);
    }

    @Override
    public CarResponse editCar(Long id, CarRequest carRequest) {
        checkCarExists(carRequest);
        Car editingCar = getCar(id);
        modelMapper.map(carRequest, editingCar);
        carRepository.save(editingCar);
        return modelMapper.map(editingCar, CarResponse.class);
    }

    @Override
    public void deleteCar(Long id) {
        carRepository.delete(getCar(id));
    }

    public Car getCar(Long id) {
        Optional<Car> optionalCar = carRepository.findById(id);
        if (optionalCar.isEmpty()) {
            throw new CarNotFoundException();
        }
        return optionalCar.get();
    }

    public void checkCarExists(CarRequest carRequest) {
        if (carRepository.existsByNumber(carRequest.getNumber())) {
            throw new PhoneAlreadyExistsException();
        }
    }
}
