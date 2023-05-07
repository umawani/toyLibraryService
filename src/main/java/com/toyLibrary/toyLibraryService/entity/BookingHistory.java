package com.toyLibrary.toyLibraryService.entity;

import jakarta.persistence.*;

import java.sql.Date;

@Entity
@Table(name = "booking_history", schema = "mydb", catalog = "")
public class BookingHistory {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private Users userId;
    @ManyToOne
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    private Product productId;
    @Basic
    @Column(name = "booked_from")
    private Date bookedFrom;
    @Basic
    @Column(name = "booked_until")
    private Date bookedUntil;

    public BookingHistory() {
    }

    public BookingHistory(Users userId, Product productId, Date bookedFrom, Date bookedUntil) {
        this.userId = userId;
        this.productId = productId;
        this.bookedFrom = bookedFrom;
        this.bookedUntil = bookedUntil;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Users getUserId() {
        return userId;
    }

    public void setUserId(Users userId) {
        this.userId = userId;
    }

    public Product getProductId() {
        return productId;
    }

    public void setProductId(Product productId) {
        this.productId = productId;
    }

    public Date getBookedFrom() {
        return bookedFrom;
    }

    public void setBookedFrom(Date bookedFrom) {
        this.bookedFrom = bookedFrom;
    }

    public Date getBookedUntil() {
        return bookedUntil;
    }

    public void setBookedUntil(Date bookedUntil) {
        this.bookedUntil = bookedUntil;
    }

}
