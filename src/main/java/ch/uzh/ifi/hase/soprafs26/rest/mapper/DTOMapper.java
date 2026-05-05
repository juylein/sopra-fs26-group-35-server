package ch.uzh.ifi.hase.soprafs26.rest.mapper;

import ch.uzh.ifi.hase.soprafs26.entity.*;
import ch.uzh.ifi.hase.soprafs26.rest.dto.*;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

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

    @AfterMapping
    default void customizeUserMapping(User user, @MappingTarget UserGetDTO dto) {
        dto.setFriends(
            user.getFriends().stream()
                .map(f -> {
                    UserGetDTO d = new UserGetDTO();
                    d.setId(f.getId());
                    d.setUsername(f.getUsername());
                    d.setBio(f.getBio());
                    return d;
                })
                .toList()
        );
    }

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
    @Mapping(target = "friends", ignore = true)
    UserGetDTO convertEntityToUserGetDTO(User user);

    @Mapping(source = "user.id", target = "id")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "user.booksRead", target = "booksRead")
    @Mapping(source = "user.pagesRead", target = "pagesRead")
    @Mapping(source = "user.numFriends", target = "numFriends")
    @Mapping(source = "leaderboard.totalPoints", target = "totalPoints")
    @Mapping(source = "leaderboard.readingPoints", target = "readingPoints")
    @Mapping(source = "leaderboard.quizzPoints", target = "quizzPoints")
    UserStatsGetDTO convertToUserStatsGetDTO(User user, Leaderboard leaderboard);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "authors", target = "authors")
    @Mapping(source = "pages", target = "pages")
    @Mapping(source = "releaseYear", target = "releaseYear")
    @Mapping(source = "genre", target = "genre")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "coverUrl", target = "coverUrl")
    @Mapping(target = "averageRating", ignore = true)
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
    @Mapping(target = "bookId", expression = "java(getBookId(session))")
    @Mapping(target = "bookTitle", expression = "java(getBookTitle(session))")
    @Mapping(target = "coverUrl", expression = "java(getBookCoverUrl(session))")
    SessionGetDTO convertSessionToGetDTO(Session session);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "type", target = "type")
    @Mapping(source = "message", target = "message")
    @Mapping(source = "referenceId", target = "referenceId")
    @Mapping(source = "read", target = "read")
    @Mapping(source = "createdAt", target = "createdAt")
    NotificationGetDTO convertNotificationEntityToGetDTO(Notifications notification);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "dateTime", target = "timestamp") 
    @Mapping(source = "rating", target = "rating")
    @Mapping(source = "review", target = "review")
    @Mapping(source = "id", target = "id")
    ReviewGetDTO convertReviewToGetDTO(Reviews review);
    List<ReviewGetDTO> convertReviewEntitiesToGetDTOs(List<Reviews> reviews);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "requester.id", target = "requesterId")
    @Mapping(source = "recipient.id", target = "recipientId")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "resolvedAt", target = "resolvedAt")
    FriendRequestGetDTO convertFriendRequestToGetDTO(FriendRequest friend_request);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "userA", target = "userA")
    @Mapping(source = "userB", target = "userB")
    @Mapping(source = "since", target = "since")
    FriendshipGetDTO convertFriendshipToGetDTO(Friendships friend_request);


    // Helper Methods to extract the values manually as Session doesn't have bookId/bookTitle/coverUrl as direct fields (nested inside a List)
    default String getBookId(Session session) {
        if (session.getParticipants().isEmpty()) return null;
        return session.getParticipants().get(0).getShelfBook().getBook().getId();
    }

    default String getBookTitle(Session session) {
        if (session.getParticipants().isEmpty()) return null;
        return session.getParticipants().get(0).getShelfBook().getBook().getName();
    }

    default String getBookCoverUrl(Session session) {
        if (session.getParticipants().isEmpty()) return null;
        return session.getParticipants().get(0).getShelfBook().getBook().getCoverUrl();
    }
}
