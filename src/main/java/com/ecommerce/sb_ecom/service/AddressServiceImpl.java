package com.ecommerce.sb_ecom.service;

import com.ecommerce.sb_ecom.exceptions.ResourceNotFoundException;
import com.ecommerce.sb_ecom.model.Address;
import com.ecommerce.sb_ecom.model.User;
import com.ecommerce.sb_ecom.payload.AddressDTO;
import com.ecommerce.sb_ecom.repository.AddressRepository;
import com.ecommerce.sb_ecom.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressServiceImpl implements AddressService{

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    AddressRepository addressRepository;

    @Autowired
    UserRepository userRepository;


    @Override
    public AddressDTO createAddress(AddressDTO addressDTO, User user) {
        Address address=modelMapper.map(addressDTO,Address.class);

        List<Address> addressList= user.getAddresses();
        user.setAddresses(addressList);


        address.setUser(user);
        Address savedAddress=addressRepository.save(address);

        return modelMapper.map(savedAddress,AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getAddresses() {

        List<Address> addresses=addressRepository.findAll();
        List<AddressDTO> addressDTOList = addresses.stream()
                .map(address->
                    modelMapper.map(address,AddressDTO.class)
                ).collect(Collectors.toList());

        return addressDTOList;
    }

    @Override
    public AddressDTO getAddressById(Long addressId) {
        Address address=addressRepository.findById(addressId)
                .orElseThrow(()->new ResourceNotFoundException("Address","addressId",addressId));

        return modelMapper.map(address,AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getUserAddresses(User user) {
        List<Address> addresses=user.getAddresses();
        List<AddressDTO> addressDTOList = addresses.stream()
                .map(address->
                        modelMapper.map(address,AddressDTO.class)
                ).collect(Collectors.toList());
    return addressDTOList;
    }

    @Override
    public AddressDTO updateAddressById(Long addressId, AddressDTO addressDTO) {
        Address address=addressRepository.findById(addressId)
                .orElseThrow(()->new ResourceNotFoundException("Address","addressId",addressId));

        address.setCity(addressDTO.getCity());
        address.setZipCode(addressDTO.getZipCode());
        address.setState(addressDTO.getState());
        address.setCountry(addressDTO.getCountry());
        address.setStreet(addressDTO.getStreet());
        address.setBuildingName(addressDTO.getBuildingName());

        Address updatedAddress=addressRepository.save(address);
        User user=address.getUser();
        user.getAddresses().removeIf(addressObject->addressObject.getAddressId().equals(addressId));
        user.getAddresses().add(updatedAddress);

        userRepository.save(user);

        return  modelMapper.map(updatedAddress,AddressDTO.class);

    }

    @Override
    public String deleteAddressById(Long addressId) {
        Address address=addressRepository.findById(addressId)
                .orElseThrow(()->new ResourceNotFoundException("Address","addressId",addressId));

        User user=address.getUser();

        user.getAddresses().removeIf(addressObject->addressObject.getAddressId().equals(addressId));
        userRepository.save(user);
        addressRepository.delete(address);

        return "Address deleted with addressId :-"+ addressId;
    }
}
