package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.entity.Shelf;

import ch.uzh.ifi.hase.soprafs26.rest.dto.ShelfGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ShelfPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.BookPostDTO;

import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;

import ch.uzh.ifi.hase.soprafs26.service.LibraryService;

import ch.uzh.ifi.hase.soprafs26.constant.BookStatus;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/library")
public class LibraryController {

    private final LibraryService libraryService;

    public LibraryController(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @GetMapping("/shelves")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<ShelfGetDTO> getLibrary(@PathVariable Long userId) {
        return DTOMapper.INSTANCE.convertShelfEntitiesToGetDTOs(libraryService.getLibrary(userId));
    }

    @PostMapping("/shelves")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ShelfGetDTO addShelf(
            @PathVariable Long userId,
            @RequestBody ShelfPostDTO shelfPostDTO) {
        Shelf shelf = libraryService.addShelf(userId, shelfPostDTO.getName());
        return DTOMapper.INSTANCE.convertShelfEntityToGetDTO(shelf);
    }

    @PostMapping("/shelves/{shelfId}/books")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ShelfGetDTO addBookToShelf(
            @PathVariable Long userId,
            @PathVariable Long shelfId,
            @RequestBody BookPostDTO bookPostDTO) {
        Shelf shelf = libraryService.addBookToShelf(userId, shelfId, bookPostDTO);
        return DTOMapper.INSTANCE.convertShelfEntityToGetDTO(shelf);
    }

    @PutMapping("/shelves/{shelfId}/books/{bookId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateBookStatus(@PathVariable Long shelfId, @PathVariable String bookId, @RequestParam BookStatus status){
        libraryService.updateBookStatus(shelfId, bookId, status);
    }
}