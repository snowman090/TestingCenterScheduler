package core.event;

import core.event.dao.AppointmentDao;
import core.event.dao.CourseDao;
import core.service.TestingCenterInfoRetrieval;
import core.user.dao.InstructorDao;
import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@Entity
@Table(name = "Exam")
public class Exam {

    @Id
    @Column (name = "exam_id")
    protected String examId;

    @Basic(optional = false)
    @Column(name = "exam_name")
    protected String examName;    // CSE308-01_1158_ex2 or "Math Placement"

    @Enumerated(EnumType.STRING)
    @Basic(optional = false)
    protected ExamType examType = ExamType.REGULAR;        // REGULAR, or AD_HOC

    @Enumerated(EnumType.STRING)
    @Basic(optional = false)
    protected ExamStatusType statusType = ExamStatusType.PENDING;  // PENDING, DENIED, or APPROVED

    @Basic(optional = false)
    @Column(name = "capacity")
    protected int capacity;           // Number of Students

    @Basic(optional = false)
    @Column(name = "term")
    protected int term;           // 1158 for Fall 2015 or 0 for ad hoc exam

    @Temporal(TemporalType.TIMESTAMP)
    @Basic(optional = false)
    @Type(type = "org.hibernate.type.LocalDateTimeType")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    protected LocalDateTime startDateTime; // start time of an exam

    @Temporal(TemporalType.TIMESTAMP)
    @Basic(optional = false)
    @Type(type = "org.hibernate.type.LocalDateTimeType")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    protected LocalDateTime endDateTime;   // end time of an exam

    @Basic(optional = false)
    @Column(name = "instructor_id" )
    protected String instructorId;    // NetId of the person who made this appointment

//    @Basic(optional = false)
    @Column(name = "course_id" )
    protected String courseId;    // CSE308-01_1158 or "adhoc" for ad hoc exam

    @Basic(optional = false)
    @Column(name = "duration")
    protected int duration = 10;;   // Duration in minute

    @Transient
    @Autowired
    protected CourseDao courseDao;

    @Transient
    @Autowired
    protected InstructorDao instructorDao;

    @Transient
    @Autowired
    protected AppointmentDao appointmentDao;

    @Transient
    protected int numAttended = 0;;

    @Transient
    protected int numAppointments = 0;// num of students who have made appointments

    @Transient
    protected double attendance  = 0.0;// Calculate on the fly.

    //the number of times of exams needed for all student to take this exam
    @Transient
    protected int numRemainingTime = 0;

    public Exam() {
        this.duration = 10;
        this.examId = "Default";
        this.examName = "Default_Exam";
        this.examType = ExamType.REGULAR;
        this.statusType = ExamStatusType.PENDING;
        this.capacity = 100;
        this.term = 1158;
        this.startDateTime = LocalDateTime.now();
        this.endDateTime = LocalDateTime.now().plusDays(1);
        this.instructorId = "Default_Instructor";
        this.courseId = "Default_Course";
    }

    /**
     *
     * @param Id ExamID
     * @param type Type
     * @param start Start Time
     * @param end End Time
     * @param duration Duration
     * @param numApp Num of Application
     * @param numNeed Num of Needed
     */
    public Exam(String Id,
                ExamType type,
                LocalDateTime start,
                LocalDateTime end,
                int duration,
                int numApp,
                int numNeed){
        this();
        examId = Id;
        this.examType = type;
        this.startDateTime = start;
        this.endDateTime = end;
        this.duration = duration;

        numAppointments = numApp;
        capacity = numNeed;
    }

    /**
     * Use this one in case of error
     * @param Id ExamID
     * @param type Type
     * @param start Start Time
     * @param end End Time
     * @param duration Duration
     * @param numApp Num of Application
     * @param numNeed Num of Needed
     * @param instructorId Instructor Id
     */
    public Exam(String Id,
                ExamType type,
                LocalDateTime start,
                LocalDateTime end,
                int duration,
                int numApp,
                int numNeed,
                String instructorId) {
        this(Id, type, start, end, duration, numApp, numNeed);
        this.instructorId = instructorId;
        this.numAttended = 0;
    }

    /**
     * Add Regular Exam
     * @param examId
     * @param examName
     * @param capacity
     * @param term
     * @param startDateTime
     * @param endDateTime
     * @param instructorId
     * @param courseId
     * @param duration
     */
    public Exam(String examId,
                String examName,
                int capacity,
                int term,
                LocalDateTime startDateTime,
                LocalDateTime endDateTime,
                String instructorId,
                String courseId,
                int duration) {//duration is minutes
        this();
        this.examId = examId;
        this.examName = examName;
        this.examType = ExamType.REGULAR;
        this.statusType = ExamStatusType.PENDING;
        this.capacity = capacity;
        this.term = term;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.instructorId = instructorId;
        this.courseId = courseId;
        this.duration = duration;
    }

    // Valid Time A Exam can use
    public int getActualDuration(){
        int actualDuration;
        TestingCenterInfoRetrieval tcir = new TestingCenterInfoRetrieval();
        TestingCenterInfo tci = tcir.findByTerm(tcir.getCurrentTerm().getTermId());
        int numSeats = tci.getNumSeats();
        int gap = tci.getGap();
        int pastDuration = 0;
        if(LocalDateTime.now().isAfter(startDateTime)){
            List<Appointment> appts = appointmentDao.findAllAppointmentByTime(LocalDateTime.now());
            //TODO TODO
            Appointment appt = new Appointment();
            for(int i = 0; i < appts.size(); i++){
                Appointment apptIter = appts.get(i);
                if(apptIter.getExamId().equals(examId)){
                    appt = apptIter;
                }
            }
            pastDuration = (int)ChronoUnit.MINUTES.between(appt.getStartDateTime(), startDateTime);
        }
        numRemainingTime = (capacity - numAttended) / numSeats;
        actualDuration = pastDuration + ( numRemainingTime + 1 ) * duration + numRemainingTime * gap;
        return actualDuration;
    }





    public double getAttendance() {
        return attendance;
    }

    public void setAttendance(int attendance) {
        this.attendance = attendance;
    }

    public String getInstructorId() {
        return instructorId;
    }

    public void setInstructorId(String instructorId) {
        this.instructorId = instructorId;
    }

    public LocalDateTime getStartDateTime(){
        return startDateTime;
    }

    public LocalDateTime getEndDateTime(){
        return endDateTime;
    }
    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getExamId() {
        return examId;
    }

    public void setExamId(String examId) {
        this.examId = examId;
    }

    public String getExamName() {
        return examName;
    }

    public void setExamName(String examName) {
        this.examName = examName;
    }

    public int getNumAppointments() {
        return numAppointments;
    }

    public void setNumAppointments(int numAppointments) {
        this.numAppointments = numAppointments;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int numStudentNeed) {
        this.capacity = numStudentNeed;
    }

    public ExamStatusType getStatusType() {
        return statusType;
    }

    public void setStatusType(ExamStatusType statusType) {
        this.statusType = statusType;
    }

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
    }

    public int getNumAttended() {
        return numAttended;
    }

    public void setNumAttended(int numAttended) {
        this.numAttended = numAttended;
    }

    public void setAttendance(double attendance) {
        this.attendance = attendance;
    }

    public ExamType getExamType() {
        return examType;
    }

    public void setExamType(ExamType type) {
        this.examType = type;
    }


    public int getDuration() { return duration;    }

    public int getDayDuration() {
        return (int)ChronoUnit.DAYS.between(startDateTime.toLocalDate(), endDateTime.toLocalDate());
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public int getNumRemainingTime() {
        return numRemainingTime;
    }

    public void setNumRemainingTime(int numRemainingTime) {
        this.numRemainingTime = numRemainingTime;
    }


// Legacy Code

//    public String getMadeBy() {
//        Instructor instructor =
//                instructorDao.findByNetID(this.instructorId);
//
//        return instructor.getLastName() + ", " + instructor.getFirstName();
//    }

//    public double getDuration()
//    {
//        return (double)ChronoUnit.MINUTES.between(getStartDateTime(), getEndDateTime())/60;
//    }

//    public LocalDateTime getStartDateTime() {// date to
//        Instant instant = Instant.ofEpochMilli(startDateTime.getTime());
//        LocalDateTime res =
//                LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
//
//        return res;
//    }
//
//    public void setStartDateTime(LocalDateTime startDateTime) {
//        Instant instant =
//                startDateTime.atZone(ZoneId.systemDefault()).toInstant();
//        this.startDateTime = Date.from(instant);
//    }
//
//    public LocalDateTime getEndDateTime() {
//        Instant instant = Instant.ofEpochMilli(endDateTime.getTime());
//        LocalDateTime res =
//                LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
//
//        return res;
//    }
//
//    public void setEndDateTime(LocalDateTime endDateTime) {
//        Instant instant =
//                endDateTime.atZone(ZoneId.systemDefault()).toInstant();
//        this.endDateTime = Date.from(instant);
//    }
}
