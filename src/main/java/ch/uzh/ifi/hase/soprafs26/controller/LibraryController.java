 package ch.uzh.ifi.hase.soprafs26.controller;

 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.web.bind.annotation.GetMapping;
 import org.springframework.web.bind.annotation.RequestHeader;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RestController;
 import org.springframework.web.server.ResponseStatusException;

 import ch.uzh.ifi.hase.soprafs26.entity.User;
 import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
 import ch.uzh.ifi.hase.soprafs26.rest.dto.LibraryDTO;
 import ch.uzh.ifi.hase.soprafs26.service.LibraryService;

 @RestController
 @RequestMapping("/library")
 public class LibraryController {

     private final LibraryService libraryService;
     private final UserRepository userRepository;

     public LibraryController(LibraryService libraryService,
                              UserRepository userRepository) {
         this.libraryService = libraryService;
         this.userRepository = userRepository;
     }

     @GetMapping
     public ResponseEntity<LibraryDTO> getLibrary(
             @RequestHeader("Authorization") String token) {

         User user = userRepository.findByToken(token);
         if (user == null) {
             throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
         }

         return ResponseEntity.ok(libraryService.getLibrary(user));
     }
 }