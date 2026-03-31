package ch.uzh.ifi.hase.soprafs26.controller;
import ch.uzh.ifi.hase.soprafs26.entity.Shelf;
import ch.uzh.ifi.hase.soprafs26.rest.dto.BookPostDTO;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ShelfGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ShelfPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.LibraryService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/library")
public class LibraryController {

    private final LibraryService libraryService;
    private final UserRepository userRepository;

    public LibraryController(LibraryService libraryService,
                            UserRepository userRepository) {
        this.libraryService = libraryService;
        this.userRepository = userRepository;
    }

    @GetMapping("/shelves")
    @ResponseStatus(HttpStatus.OK)
    public List<ShelfGetDTO> getLibrary(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String token) {

        User user = userRepository.findByToken(token);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
        //ensures that the user returned by findByToken is the same one trying to access the library
        if (!user.getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        return DTOMapper.INSTANCE.convertShelfEntitiesToGetDTOs(libraryService.getLibrary(user));
    }

    @PostMapping("/shelves")
    @ResponseStatus(HttpStatus.CREATED)
    public ShelfGetDTO addShelf(            
            @PathVariable Long userId,
            @RequestHeader("Authorization") String token, 
            @RequestBody ShelfPostDTO shelfPostDTO) {

        User user = userRepository.findByToken(token);
        if (user == null){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }

         if (!user.getId().equals(userId)) {
          throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        return DTOMapper.INSTANCE.convertShelfEntityToGetDTO(libraryService.addShelf(user, shelfPostDTO.getName()));
    }

    @PostMapping("/shelves/{shelfId}/books")
    @ResponseStatus(HttpStatus.CREATED)
    public ShelfGetDTO addBookToShelf(
            @PathVariable Long userId,
            @PathVariable Long shelfId,
            @RequestHeader("Authorization") String token,
            @RequestBody BookPostDTO bookPostDTO) {

        User user = userRepository.findByToken(token);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
        if (!user.getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        Shelf shelf = libraryService.addBookToShelf(user, shelfId, bookPostDTO);
        return DTOMapper.INSTANCE.convertShelfEntityToGetDTO(shelf);
    }

}