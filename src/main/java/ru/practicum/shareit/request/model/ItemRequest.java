package ru.practicum.shareit.request.model;

import jakarta.persistence.*;

/**
 * TODO Sprint add-item-requests.
 */
@Entity
@Table(name = "requests")
public class ItemRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
