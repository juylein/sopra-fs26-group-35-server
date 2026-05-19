package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.Book;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.Shelf;
import ch.uzh.ifi.hase.soprafs26.entity.ShelfBook;
import ch.uzh.ifi.hase.soprafs26.entity.ShelfInvitation;

import ch.uzh.ifi.hase.soprafs26.repository.ShelfRepository;
import ch.uzh.ifi.hase.soprafs26.repository.BookRepository;
import ch.uzh.ifi.hase.soprafs26.repository.ShelfBookRepository;
import ch.uzh.ifi.hase.soprafs26.repository.ShelfInvitationRepository;

import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.BookPostDTO;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.uzh.ifi.hase.soprafs26.service.ActivitiesService;

import ch.uzh.ifi.hase.soprafs26.constant.BookStatus;
import ch.uzh.ifi.hase.soprafs26.constant.NotificationType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LibraryService {

    private final ShelfRepository shelfRepository;
    private final BookRepository bookRepository;
    private final ShelfBookRepository shelfbookRepository;
    private final ActivitiesService activitiesService;
    private final UserRepository userRepository;
    private final ShelfInvitationRepository shelfInvitationRepository;
    private final NotificationService notificationService;
    private static final List<String> STATUS_SHELF_NAMES = List.of("To Read", "Recent Readings", "Read");

    @Autowired
    public LibraryService(ShelfRepository shelfRepository,
                          BookRepository bookRepository,
                          ShelfBookRepository shelfbookRepository,
                          UserRepository userRepository,
                          ActivitiesService activitiesService,
                          ShelfInvitationRepository shelfInvitationRepository,
                          NotificationService notificationService
                        ) {
      this.shelfRepository = shelfRepository;
      this.bookRepository = bookRepository;
      this.userRepository = userRepository;
      this.shelfbookRepository = shelfbookRepository;
      this.activitiesService = activitiesService;
      this.shelfInvitationRepository = shelfInvitationRepository;
      this.notificationService = notificationService;
    }

    private User getAuthenticatedUser(Long userId) {
        String currentUserToken = (String) SecurityContextHolder.getContext().getAuthentication().getCredentials();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User with id " + userId + " not found"));
        if (!user.getToken().equals(currentUserToken)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You are not allowed to perform this action for this user");
        }
        return user;
    }

    private User getUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User with id " + userId + " not found"));

        // TODO check if user is the authenticated or a friend of the authenticated
        return user;
    }

    public List<Shelf> getLibrary(Long userId) {
        User user = getUser(userId);
        return user.getShelves();
    }

    public Shelf addShelf(Long userId, String name) {
        User user = getAuthenticatedUser(userId);

        Shelf shelf = new Shelf();
        shelf.setName(name);
        shelf.setOwner(user);
        shelf.setShared(false);

        return shelfRepository.save(shelf);
    }

    public Shelf addBookToShelf(Long userId, Long shelfId, BookPostDTO bookPostDTO) {
        User user = getAuthenticatedUser(userId);

        Shelf shelf = shelfRepository.findById(shelfId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shelf not found"));

        boolean isOwner = shelf.getOwner() != null && shelf.getOwner().getId().equals(user.getId());
        boolean isMember = shelf.getOwners().contains(user);

        if (!isOwner && !isMember){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        Book book = bookRepository.findById(bookPostDTO.getGoogleId()).orElseGet(() -> {
            Book b = new Book();
            b.setId(bookPostDTO.getGoogleId());
            b.setName(bookPostDTO.getName());
            b.setAuthors(bookPostDTO.getAuthors());
            b.setPages(bookPostDTO.getPages());
            b.setReleaseYear(bookPostDTO.getReleaseYear());
            b.setGenre(bookPostDTO.getGenre());
            b.setDescription(bookPostDTO.getDescription());
            b.setCoverUrl(bookPostDTO.getCoverUrl());
            return bookRepository.save(b);
        });

        // Prevent duplicate ShelfBook entries
        boolean alreadyOnShelf = shelf.getBooks().stream()
                .anyMatch(sb -> sb.getBook().getId().equals(book.getId()));

        if (alreadyOnShelf) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Book already on this shelf");
        }

        shelf.addBook(book);
        Shelf savedShelf = shelfRepository.save(shelf);

        // Create activity when a book lands on a status shelf via direct shelf add
        String shelfName = shelf.getName();
        if ("Recent Readings".equals(shelfName) || "Read".equals(shelfName)) {
            BookStatus statusForShelf = "Recent Readings".equals(shelfName) ? BookStatus.READING : BookStatus.FINISHED;
            // Update the persisted ShelfBook's status to match the shelf
            savedShelf.getBooks().stream()
                .filter(sb -> sb.getBook().getId().equals(book.getId()))
                .findFirst()
                .ifPresent(sb -> {
                    sb.setStatus(statusForShelf);
                    shelfbookRepository.save(sb);
                });
            activitiesService.addActivity(user, statusForShelf, book);
        }

        return savedShelf;
    }

    public void deleteBookfromShelf(Long shelfId, String bookId, Long userId){
        Shelf shelf = shelfRepository.findById(shelfId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shelf not found"));

        User requestingUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        
        boolean isOwner = shelf.getOwner() != null && shelf.getOwner().getId().equals(requestingUser.getId());
        boolean isMember = shelf.getOwners().contains(requestingUser);

        if(!isOwner && !isMember){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        ShelfBook shelfBook = shelfbookRepository.findByShelfIdAndBookId(shelfId, bookId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found on this shelf"));

        shelfbookRepository.delete(shelfBook);
    }

    public ShelfBook updateBookStatus(Long shelfId, String bookId, BookStatus newStatus){
        Shelf shelf = shelfRepository.findById(shelfId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shelf not found"));

        String currentToken = (String) SecurityContextHolder.getContext()
                .getAuthentication().getCredentials();

        boolean isAuthorized;

        if (shelf.getShared()) { // Shared shelf -> check if the requester is one of the members
            isAuthorized = shelf.getOwners().stream()
                    .anyMatch(u -> u.getToken().equals(currentToken));
        } else { // Private shelf -> check if the requester is the owner
            isAuthorized = shelf.getOwner().getToken().equals(currentToken);
        }

        if (!isAuthorized) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        
        ShelfBook shelfBook = shelfbookRepository.findByShelfIdAndBookId(shelfId, bookId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found on this shelf"));

        BookStatus previousStatus = shelfBook.getStatus();
        shelfBook.setStatus(newStatus);

        User user = shelf.getShared() //Find the right user for the activity log
                ? shelf.getOwners().stream()
                .filter(u -> u.getToken().equals(currentToken))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied"))
                : shelf.getOwner(); // Private shelf -> just use the owner directly

        Book book = shelfBook.getBook();

        String targetShelfName = newStatus == BookStatus.READING ? "Recent Readings"
                               : newStatus == BookStatus.FINISHED ? "Read"
                               : "To Read";

        // Move book between status shelves only when necessary
        List<String> statusShelfNames = List.of("To Read", "Recent Readings", "Read");
        Optional<ShelfBook> existingStatusEntry = shelfbookRepository
            .findByShelf_OwnerIdAndBookIdAndShelf_NameIn(user.getId(), bookId, statusShelfNames);

        boolean alreadyOnTargetShelf = existingStatusEntry.isPresent()
            && existingStatusEntry.get().getShelf().getName().equals(targetShelfName);

        if (!alreadyOnTargetShelf) {
            // Remove from whichever status shelf it's currently on (if any)
            existingStatusEntry.ifPresent(shelfbookRepository::delete);

            // Add to the target status shelf
            Shelf targetShelf = user.getShelves().stream()
                .filter(s -> targetShelfName.equals(s.getName()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, targetShelfName + " shelf not found"));
            targetShelf.addBook(book);
            shelfRepository.save(targetShelf);
        }

        // Only save shelfBook if it wasn't the entity we just deleted
        boolean shelfBookWasDeleted = existingStatusEntry.isPresent()
            && !alreadyOnTargetShelf
            && existingStatusEntry.get().getId().equals(shelfBook.getId());

        if (!shelfBookWasDeleted) {
            shelfbookRepository.save(shelfBook);
        }

        // Only create activity when status actually changes to avoid duplicates
        if (previousStatus != newStatus) {
            activitiesService.addActivity(user, newStatus, book);
        }

        return shelfBook;
    }

    public void deleteShelf(Long userId, Long shelfId){
        User user = getAuthenticatedUser(userId);
        Shelf shelf = shelfRepository.findById(shelfId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shelf not found"));

        if (shelf.getShared()) {
            if (!shelf.getOwners().contains(user)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
            }
        } else {
            if (!shelf.getOwner().getId().equals(user.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
            }
        }

        shelfRepository.delete(shelf);
    }

    public void renameShelf(Long userId, Long shelfId, String newName) {
        User user = getAuthenticatedUser(userId);

        Shelf shelf = shelfRepository.findById(shelfId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shelf not found"));

        if (shelf.getShared()) {
            if (!shelf.getOwners().contains(user)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
            }
        } else {
            if (!shelf.getOwner().getId().equals(user.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
            }
        }

        shelf.setName(newName);
        shelfRepository.save(shelf);
    }

    public void inviteToShelf(Long userId, Long shelfId, Long targetUserId) {
        User requester = getAuthenticatedUser(userId);

        Shelf shelf = shelfRepository.findById(shelfId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shelf not found"));

        if (STATUS_SHELF_NAMES.contains(shelf.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Status shelves (To Read, Recent Readings, Read) cannot be shared");
        }

        boolean isPrivateOwner = !shelf.getShared()
                && shelf.getOwner() != null
                && shelf.getOwner().getId().equals(requester.getId());
        boolean isSharedMember = shelf.getShared() && shelf.getOwners().contains(requester);

        if (!isPrivateOwner && !isSharedMember) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User with id " + targetUserId + " not found"));

        if (!requester.getFriends().contains(target)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only invite friends to shared shelves");
        }

        boolean alreadyMember = (shelf.getOwner() != null && shelf.getOwner().getId().equals(target.getId()))
                || shelf.getOwners().contains(target);
        if (alreadyMember) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already a member of this shelf");
        }

        if (shelfInvitationRepository.existsByShelfIdAndRecipientIdAndStatus(shelfId, target.getId(), "PENDING")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An invitation is already pending for this user");
        }

        ShelfInvitation invitation = new ShelfInvitation();
        invitation.setSender(requester);
        invitation.setRecipient(target);
        invitation.setShelf(shelf);
        ShelfInvitation saved = shelfInvitationRepository.save(invitation);

        notificationService.createNotification(
                target.getId(),
                NotificationType.SHELF_INVITATION,
                requester.getUsername() + " invited you to join the shelf \"" + shelf.getName() + "\"",
                saved.getId()
        );
    }

    public List<ShelfInvitation> getIncomingShelfInvitations(Long userId) {
        User user = getAuthenticatedUser(userId);
        return shelfInvitationRepository.findByRecipientIdAndStatus(user.getId(), "PENDING");
    }

    public void acceptShelfInvitation(Long userId, Long invitationId) {
        User recipient = getAuthenticatedUser(userId);

        ShelfInvitation invitation = shelfInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation not found"));

        if (!invitation.getRecipient().getId().equals(recipient.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        if (!"PENDING".equals(invitation.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Invitation is no longer pending");
        }

        Shelf shelf = invitation.getShelf();

        if (!shelf.getShared()) {
            // First acceptance: convert private shelf to shared
            shelf.setShared(true);
            shelf.getOwners().add(shelf.getOwner());
            shelf.setOwner(null);
        }

        shelf.getOwners().add(recipient);
        shelfRepository.save(shelf);

        invitation.setStatus("ACCEPTED");
        invitation.setResolvedAt(LocalDateTime.now());
        shelfInvitationRepository.save(invitation);
    }

    public void rejectShelfInvitation(Long userId, Long invitationId) {
        User recipient = getAuthenticatedUser(userId);

        ShelfInvitation invitation = shelfInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation not found"));

        if (!invitation.getRecipient().getId().equals(recipient.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        if (!"PENDING".equals(invitation.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Invitation is no longer pending");
        }

        invitation.setStatus("REJECTED");
        invitation.setResolvedAt(LocalDateTime.now());
        shelfInvitationRepository.save(invitation);
    }

    public List<Shelf> getSharedShelves(Long userId) {
        User user = getAuthenticatedUser(userId);
        return new ArrayList<>(user.getSharedShelves());
    }
}