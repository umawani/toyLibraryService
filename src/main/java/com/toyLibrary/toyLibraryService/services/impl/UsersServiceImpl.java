package com.toyLibrary.toyLibraryService.services.impl;

import com.toyLibrary.toyLibraryService.constants.UserTypes;
import com.toyLibrary.toyLibraryService.dto.request.LoginRequestDTO;
import com.toyLibrary.toyLibraryService.dto.request.RegistrationRequestDTO;
import com.toyLibrary.toyLibraryService.dto.response.LoginResponseDTO;
import com.toyLibrary.toyLibraryService.dto.response.ResponseDTO;
import com.toyLibrary.toyLibraryService.dto.response.UserResponseDTO;
import com.toyLibrary.toyLibraryService.entity.BookingHistory;
import com.toyLibrary.toyLibraryService.entity.Product;
import com.toyLibrary.toyLibraryService.entity.UserType;
import com.toyLibrary.toyLibraryService.entity.Users;
import com.toyLibrary.toyLibraryService.repository.BookingHistoryRepository;
import com.toyLibrary.toyLibraryService.repository.UserTypeRepository;
import com.toyLibrary.toyLibraryService.repository.UsersRepository;
import com.toyLibrary.toyLibraryService.services.BookingHistoryService;
import com.toyLibrary.toyLibraryService.services.ProductService;
import com.toyLibrary.toyLibraryService.services.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class UsersServiceImpl implements UsersService {

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    UserTypeRepository userTypeRepository;

    @Autowired
    ProductService productService;

    @Autowired
    BookingHistoryService bookingHistoryService;

    public ResponseDTO<LoginResponseDTO> login(LoginRequestDTO req){
        Optional<Users> optionalUser = usersRepository.findByEmail(req.getEmail());
        // First check if user exists with given email
        if(optionalUser.isEmpty()){
            // User does not exist with given email
            System.out.println("User was not found! Returning Error!");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Incorrect Email/Password Combination");
        }
        // User does exist with given email, now check if the password inputted is correct
        Users user = optionalUser.get();
        if(!user.getPassword().equals(req.getPassword())){
            // Inputted password is incorrect!
            System.out.println("Incorrect password inputted! Returning Error!");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Incorrect Email/Password Combination");
        }
        // All checks passed! User exists so now need to return a token
        System.out.println("Details provided verified! Logging in...");
        return new ResponseDTO<>(new LoginResponseDTO(generateRandomAlphaNumericString(), user), HttpStatus.OK.value(), "Logged in Successfully!");
    }

    public ResponseDTO<String> registerCustomer(RegistrationRequestDTO req){
        // First check if user already exists with given email
        Optional<Users> optionalUser = usersRepository.findByEmail(req.getEmail());
        if(optionalUser.isPresent()){
            // User already exists!
            System.out.println("User already exists with provided email!");
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exists!");
        }
        Users newUser = new Users();

        UserType customerType = userTypeRepository.findById(UserTypes.CUSTOMER);

        newUser.setEmail(req.getEmail());
        newUser.setName(req.getName());
        newUser.setPassword(req.getPassword());
        newUser.setUserType(customerType);

        usersRepository.save(newUser);

        return new ResponseDTO<>(HttpStatus.OK.value(), "User Creation Successful! Please login now!");
    }


    /**
     * This code was copied off of https://www.baeldung.com/java-random-string to generate a random string to act as a
     * token for login response
     * @return random alphanumeric string
     */
    private String generateRandomAlphaNumericString(){
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        return generatedString;
    }

    public ResponseDTO<UserResponseDTO> addToCart(Integer userId, Integer productId){
        // First check if item is not already booked
        if(productService.checkIfProductIsAlreadyBooked(productId)){
            // Product is already booked, return error
            System.out.println("Product is already booked! Cannot add to cart!");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product is already booked by someone! Cannot add to cart!");
        }
        Optional<Users> optionalUser = usersRepository.findById(userId);
        // First check if user exists with given ID
        if(optionalUser.isEmpty()){
            // User does not exist with given ID
            System.out.println("User was not found! Returning Error!");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found with provided ID");
        }
        Users user = optionalUser.get();
        List<Product> currentCart = user.getCart();
        Product p = new Product();
        p.setId(productId);
        currentCart.add(p);
        user.setCart(currentCart);

        user = usersRepository.save(user);
        System.out.println("Product added to cart successfully!");
        return new ResponseDTO<>(new UserResponseDTO(user), HttpStatus.OK.value(), "Added item to cart successfully!");
    }

    public ResponseDTO<UserResponseDTO> removeFromCart(Integer userId, Integer productId){
        Optional<Users> optionalUser = usersRepository.findById(userId);
        // First check if user exists with given ID
        if(optionalUser.isEmpty()){
            // User does not exist with given ID
            System.out.println("User was not found! Returning Error!");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found with provided ID");
        }
        Users user = optionalUser.get();
        List<Product> currentCart = user.getCart();

        currentCart = currentCart.stream().filter(p -> p.getId() != productId).collect(Collectors.toList());
        user.setCart(currentCart);

        user = usersRepository.save(user);
        System.out.println("Product removed from cart successfully!");
        return new ResponseDTO<>(new UserResponseDTO(user), HttpStatus.OK.value(), "Product removed from cart successfully!");
    }

    public ResponseDTO<String> checkoutCart(Integer userId){
        Optional<Users> optionalUser = usersRepository.findById(userId);
        // First check if user exists with given ID
        if(optionalUser.isEmpty()){
            // User does not exist with given ID
            System.out.println("User was not found! Returning Error!");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found with provided ID");
        }
        Users user = optionalUser.get();
        List<Product> cart = user.getCart();
        List<BookingHistory> bookingHistoryList = new ArrayList<>();
        Date bookedUntil = Date.valueOf(LocalDate.now().plusMonths(1));
        cart.stream().forEach(p -> {
            p.setBookedBy(user);
            p.setBookedUntil(bookedUntil);
            bookingHistoryList.add(new BookingHistory(user, p, Date.valueOf(LocalDate.now()), bookedUntil));
        });
        bookingHistoryService.saveBookingHistories(bookingHistoryList);
        productService.updateProductBookings(cart);

        // Need to reset cart
        user.setCart(new ArrayList<>());
        usersRepository.save(user);

        return new ResponseDTO<>(HttpStatus.OK.value(), "Cart checked out!");
    }
}
