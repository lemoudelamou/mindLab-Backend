package com.example.mindLab.controllers;

import com.example.mindLab.models.Event;
import com.example.mindLab.models.Patient;
import com.example.mindLab.models.User;
import com.example.mindLab.services.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/events")
public class EventController {
    private final EventService eventService;

    @Autowired
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<Event>> getAllEvents() {
        List<Event> events = eventService.getAllEvents();
        return new ResponseEntity<>(events, HttpStatus.OK);
    }

    @GetMapping("/by-doctor/{userId}")
    public List<Event> getEventsByDoctor(@PathVariable Long userId) {
        return eventService.getAllEventsByDoctor(userId);
    }

    @GetMapping("/by-id/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        Optional<Event> event = eventService.getEventById(id);
        return event.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Event> saveEvent(@RequestBody Event event) {
        Event savedEvent = eventService.saveEvent(event);
        return new ResponseEntity<>(savedEvent, HttpStatus.CREATED);
    }


    @PutMapping("/update/{id}")
    public ResponseEntity<Event> updateEvent(@PathVariable Long id, @RequestBody Event updatedEvent) {
        Optional<Event> existingEvent = eventService.getEventById(id);

        if (existingEvent.isPresent()) {
            Event currentEvent = existingEvent.get();

            currentEvent.setTitle(updatedEvent.getTitle());
            currentEvent.setNote(updatedEvent.getNote());



            Event updatedEventEntity = eventService.saveEvent(currentEvent);
            return new ResponseEntity<>(updatedEventEntity, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
