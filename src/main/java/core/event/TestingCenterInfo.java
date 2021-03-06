package core.event;

import org.hibernate.annotations.*;
import org.hibernate.annotations.CascadeType;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "TestingCenterInfo")
public class TestingCenterInfo {
    @Id
    private int term;      // 1158

    @Basic(optional = false)
    private int numSeats;   // By default there are 64 seats in sum.

    @Basic(optional = false)
    private int numSetAsideSeats;

    @Basic(optional = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime open;

    @Temporal(TemporalType.TIMESTAMP)
    @Type(type = "org.hibernate.type.LocalTimeType")
    @Basic(optional = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime close;

    @Cascade(CascadeType.ALL)
    @ElementCollection // Testing Center is the owner of CloseDateRangeTuple
    private List<CloseDateRangeTuple> closeDateRanges;

    @Cascade(CascadeType.ALL)       // Testing Center is the owner of ETSTestTimeRangeTuple
    @ElementCollection
    private List<ETSTestTimeRangeTuple> reserveRanges;

    @Basic(optional = false)
    private int gap;

    @Basic(optional = false)
    private int reminderInterval;

    public TestingCenterInfo() {
        this(1151);
    }

    /**
     * Default Constructor,
     * 64 Seats, 4 Set Aside Seats, Open at 9am, Close at 7pm, Gap Time 10 min, Interval 10 min.
     * @param term
     */
    public TestingCenterInfo(int term){
        this(term, 60, 4,
                LocalTime.of(9, 0),LocalTime.of(17, 0),
                new ArrayList<>(), new ArrayList<>(), 10, 10);
    }
    public TestingCenterInfo(int term, int numSeats, int numSetAsideSeats, LocalTime open,
                             LocalTime close, List<CloseDateRangeTuple> closeDateRanges,
                             List<ETSTestTimeRangeTuple> reserveRanges, int gap, int reminderInterval) {
        this.term = term;
        this.numSeats = numSeats;
        this.numSetAsideSeats = numSetAsideSeats;
        this.open = open;
        this.close = close;
        this.closeDateRanges = closeDateRanges;
        this.reserveRanges = reserveRanges;
        this.gap = gap;
        this.reminderInterval = reminderInterval;
    }

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
    }

    public int getNumSeats() {
        return numSeats;
    }

    public void setNumSeats(int numSeats) {
        this.numSeats = numSeats;
    }

    public int getNumSetAsideSeats() {
        return numSetAsideSeats;
    }

    public void setNumSetAsideSeats(int numSetAsideSeats) {
        this.numSetAsideSeats = numSetAsideSeats;
    }

    public LocalTime getOpen() {
        return open;
    }

    public void setOpen(LocalTime open) {
        this.open = open;
    }

    public LocalTime getClose() {
        return close;
    }

    public void setClose(LocalTime close) {
        this.close = close;
    }

    public List<CloseDateRangeTuple> getCloseDateRanges() {
        return closeDateRanges;
    }

    public void setCloseDateRanges(List<CloseDateRangeTuple> closeDateRanges) {
        this.closeDateRanges = closeDateRanges;
    }

    public List<ETSTestTimeRangeTuple> getReserveRanges() {
        return reserveRanges;
    }

    public void setReserveRanges(List<ETSTestTimeRangeTuple> reserveRanges) {
        this.reserveRanges = reserveRanges;
    }

    public int getGap() {
        return gap;
    }

    public void setGap(int gap) {
        this.gap = gap;
    }

    public int getReminderInterval() {
        return reminderInterval;
    }

    public void setReminderInterval(int reminderInterval) {
        this.reminderInterval = reminderInterval;
    }
}