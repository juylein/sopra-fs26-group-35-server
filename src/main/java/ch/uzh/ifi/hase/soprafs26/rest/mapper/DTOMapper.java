package ch.uzh.ifi.hase.soprafs26.rest.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import ch.uzh.ifi.hase.soprafs26.entity.Book;
import ch.uzh.ifi.hase.soprafs26.entity.ShelfBook;
import ch.uzh.ifi.hase.soprafs26.entity.Leaderboard;
import ch.uzh.ifi.hase.soprafs26.entity.Shelf;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.Activities;
import ch.uzh.ifi.hase.soprafs26.entity.SessionParticipant;
import ch.uzh.ifi.hase.soprafs26.entity.Session;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ActivitiesGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.BookGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionParticipantPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ShelfBookGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ShelfBookPutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ShelfGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserStatsGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;

import java.util.List;

/**
 * DTOMapper
 * This class is responsible for generating classes that will automatically
 * transform/map the internal representation
 * of an entity (e.g., the User) to the external/API representation (e.g.,
 * UserGetDTO for getting, UserPostDTO for creating)
 * and vice versa.
 * Additional mappers can be defined for new entities.
 * Always created one mapper for getting information (GET) and one mapper for
 * creating information (POST).
 */
@Mapper
public interface DTOMapper {

    DTOMapper INSTANCE = Mappers.getMapper(DTOMapper.class);

    @Mapping(source = "name", target = "name")
    @Mapping(source = "username", target = "username")
    @Mapping(source = "bio", target = "bio")
    @Mapping(source = "password", target = "password")
    @Mapping(source = "genres", target = "genres")
    @Mapping(target = "token", expression = "java(java.util.UUID.randomUUID().toString())")
    @Mapping(target = "status", constant = "ONLINE")
    User convertUserPostDTOtoEntity(UserPostDTO userPostDTO);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "username", target = "username")
    @Mapping(source = "bio", target = "bio")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "token", target = "token")
    @Mapping(source = "genres", target = "genres")
    @Mapping(source = "creationDate", target = "creationDate")
    UserGetDTO convertEntityToUserGetDTO(User user);

    @Mapping(source = "user.id", target = "id")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "user.booksRead", target = "booksRead")
    @Mapping(source = "user.pagesRead", target = "pagesRead")
    @Mapping(source = "user.numFriends", target = "numFriends")
    @Mapping(source = "leaderboard.totalPoints", target = "totalPoints")
    UserStatsGetDTO convertToUserStatsGetDTO(User user, Leaderboard leaderboard);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "authors", target = "authors")
    @Mapping(source = "pages", target = "pages")
    @Mapping(source = "releaseYear", target = "releaseYear")
    @Mapping(source = "genre", target = "genre")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "coverUrl", target = "coverUrl")
    BookGetDTO convertBookEntityToGetDTO(Book book);

    @Mapping(source = "status", target = "status")
    ShelfBookPutDTO convertShelfBookEntityToPutDTO(ShelfBook ShelfBook);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "shared", target = "shared")
    @Mapping(source = "books", target = "shelfBooks")
    ShelfGetDTO convertShelfEntityToGetDTO(Shelf shelf);
    List<ShelfGetDTO> convertShelfEntitiesToGetDTOs(List<Shelf> shelves);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "book.name", target = "book")
    @Mapping(source = "actions", target = "actions")
    ActivitiesGetDTO convertActivitiesEntityToGetDTO(Activities activities);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "book", target = "book")
    @Mapping(source = "pagesRead", target = "pagesRead")
    ShelfBookGetDTO convertShelfBookToShelfBookGetDTO(ShelfBook shelfBook);
    List<ShelfBookGetDTO> convertShelfBookToShelfBookGetDTOs(List<ShelfBook> shelves);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "shelfBook.id", target = "shelfBookId")
    SessionParticipantPostDTO convertSessionParticipantEntityToPostDTO(SessionParticipant sessionParticipant);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "startTime", target = "startTime")
    @Mapping(source = "endTime", target = "endTime")
    SessionGetDTO convertSessionToGetDTO(Session session);
}
