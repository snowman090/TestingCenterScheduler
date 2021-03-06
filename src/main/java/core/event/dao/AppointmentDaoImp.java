package core.event.dao;

import core.event.*;
import core.service.AppointmentManageService;
import core.service.SessionManager;
import core.service.TestingCenterInfoRetrieval;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Repository
public class AppointmentDaoImp implements AppointmentDao {
    @Autowired
    private TestingCenterTimeSlotsDao tctsDao;

    public AppointmentDaoImp() {
    }

    @Override
    public List findAllAppointment() {
        Session session = SessionManager.getInstance().openSession();
        Transaction tx = session.beginTransaction();
        List appointments = session.createQuery("FROM Appointment").list();
        tx.commit();
        session.close();
        return appointments;
    }

    @Override
    public List<Appointment> findAllByStudent(String NetId) {
        Session session = SessionManager.getInstance().openSession();
        Transaction tx = null;
        List<Appointment> result = new ArrayList<>();
        try {
            tx = session.beginTransaction();
            Query query = session.createQuery
                    ("FROM Appointment A WHERE A.studentId = :stuId");
            query.setParameter("stuId", NetId);
            result = query.list();
            tx.commit();
        } catch (HibernateException he) {
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            session.close();
        }
        return result;
    }

    public List<Appointment> findAllAppointmentByTime(LocalDateTime time) {
        ArrayList<Appointment> result = new ArrayList<Appointment>();
        List allAppointment = findAllAppointment();
        Appointment appointmentIter = new Appointment();
        TestingCenterInfoRetrieval info = new TestingCenterInfoRetrieval();
        int gap = info.findByTerm(info.getCurrentTerm().getTermId()).getGap();
        for (int i = 0; i < allAppointment.size(); i++) {
            appointmentIter = (Appointment) allAppointment.get(i);
            if ((time.minusMinutes(gap).isBefore(appointmentIter.getEndDateTime()))
                    && (
                    (time.isAfter(appointmentIter.getStartDateTime()))
                            ||
                            time.isEqual(appointmentIter.getStartDateTime())
            )
                    ) {
                result.add(appointmentIter);
            }
        }
        return result;
    }

    /**
     * @param appointment
     * @return
     */
    @Override
    public boolean insertAppointment(Appointment appointment) {
        Session session = SessionManager.getInstance().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.save(appointment);
            tx.commit();
        } catch (HibernateException he) {
            if (tx != null) {
                tx.rollback();
            }
            return false;
        } finally {
            session.close();
        }
        return true;
    }

    /**
     * Update Appointment
     * @param appointment
     * @param slots
     * @return
     */
    @Override
    public boolean makeAppointment(Appointment appointment, TestingCenterTimeSlots slots) {
        Session session = SessionManager.getInstance().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.merge(slots);
            session.merge(appointment);
            tx.commit();
        } catch (HibernateException he) {
            if (tx != null) {
                tx.rollback();
            }
            return false;
        } finally {
            session.close();
        }
        return true;
    }


    @Override
    public boolean deleteAppointment(int appointmentId) {
        Session session = SessionManager.getInstance().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Appointment appointment = session.get(Appointment.class, appointmentId);
            session.delete(appointment);
            tx.commit();
        } catch (HibernateException he) {
            if (tx != null) {
                tx.rollback();
            }
            return false;
        } finally {
            session.close();
        }
        return true;
    }

    @Override
    public boolean updateAppointment(Appointment appointment) {
        Session session = SessionManager.getInstance().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.merge(appointment);
            tx.commit();
        } catch (HibernateException he) {
            if (tx != null) {
                tx.rollback();
            }
            return false;
        } finally {
            session.close();
        }
        return true;
    }

    @Override
    public List<Appointment> findAllAppointmentsByTerm(Term term) {
        Session session = SessionManager.getInstance().openSession();
        Transaction tx = null;
        List<Appointment> appointments = new ArrayList<>();
        try {
            tx = session.beginTransaction();
            Query query = session.createQuery("from Appointment a where a.startDateTime >= :startDate and :endDate >= a.endDateTime");
            query.setTimestamp("endDate", Date.from(term.getTermEndDate().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
            query.setTimestamp("startDate", Date.from(term.getTermStartDate().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
            // If query won't get any record from table, the result will be an empty list.
            appointments = query.list();
            tx.commit();
        } catch (HibernateException he) {
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            session.close();
        }
        return appointments;
    }

    @Override
    public Appointment findAppointmentById(int AppointmentID) {
        Session session = SessionManager.getInstance().openSession();
        Transaction tx = null;
        Appointment result;
        try {
            session.beginTransaction();
            Query query = session.createQuery("FROM Appointment A WHERE A.appointmentID = :appId");
            query.setParameter("appId", AppointmentID);
            result = (Appointment) query.uniqueResult();
            tx.commit();
        } catch (HibernateException he) {
            if (tx != null) {
                tx.rollback();
            }
            return new Appointment();
        } finally {
            session.close();
        }

        session.close();
        return result;
    }

    public void makeAppointment(Appointment appt) {

        if (checkLegalAppointment(appt)) {
            insertAppointment(appt);
            System.out.print("\nSuccess.");
        } else
            System.out.print("\nFail.");
    }

    /**
     * Check if the attempting appointment is valid. If not, the system will denied it automatically.
     *
     * @param a
     * @return
     */
    public boolean checkLegalAppointment(Appointment a) {

        String studentIdCheck = a.getStudentId();
        String examIdCheck = a.getExamId();
        boolean res = true;

        Session session = SessionManager.getInstance().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Exam exam = (Exam) session.get(Exam.class, examIdCheck);

            // 1. Check Student in Ad Hoc List
            if (exam.getExamType().equals(ExamType.AD_HOC)) {
                List<StudentEntry> studentEntries = ((AdhocExam) exam).getStudentList();
                for (StudentEntry se : studentEntries) {
                    if (se.getNetId().equals(studentIdCheck)) {
                        break;
                    }
                }
            } else {
                // 2. Check Student Enroll in Course
                Roster roster = (Roster) session.get(Roster.class,
                        new Roster(exam.getCourseId(), studentIdCheck, a.getTerm()));
                if (roster == null) {
                    return false;
                }
            }

            //the appt time is during exam time period
            // d. Check appointment is entirely between the start date-time and end date-time of exam.
            if ((exam.getStartDateTime().isBefore(a.getStartDateTime()))
                    && (exam.getEndDateTime().isAfter(a.getEndDateTime()))) {
                //map to object
                String hql = "FROM Appointment b WHERE b.studentId = :stuId";
                String stuId = studentIdCheck;
                Query query = session.createQuery(hql);
                query.setParameter("stuId", "%" + stuId + "%");
                List<Appointment> list = query.list();
                Iterator it = list.iterator();

                TestingCenterInfo testingCenterInfo = (TestingCenterInfo) session.
                        get(TestingCenterInfo.class, exam.getTerm());


                ArrayList<Appointment> possibleLists = new ArrayList<Appointment>();
                while (it.hasNext()) {
                    Appointment appt = new Appointment();
                    appt = (Appointment) it.next();
                    // b. Does not have an existing appointment for the same exam.
                    if (!appt.getExamId().equals(examIdCheck)) {
                        //c. Student does not have an appointment for a different exam
                        // in an overlapping timeslot and gap
                        if (((a.getStartDateTime().minusMinutes(testingCenterInfo.getGap())).isAfter(appt.getEndDateTime())) ||
                                (a.getEndDateTime().isBefore((appt.getStartDateTime().minusMinutes(testingCenterInfo.getGap()))))) {
                            //continue
                            possibleLists.add(appt);
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }

                Appointment apptIter;
                int i = 0;
                while (i < possibleLists.size()) {
                    apptIter = possibleLists.get(i);
                    // E. Non-set-aside seat is available
                    String timeSlotId = Integer.toString(a.getStartDateTime().getDayOfYear()) +
                            Integer.toString(a.getEndDateTime().getHour()) + Integer.toString(a.
                            getStartDateTime().getMinute());
                    TestingCenterTimeSlots testingCenterTimeSlots = (TestingCenterTimeSlots)
                            session.get(TestingCenterTimeSlots.class, timeSlotId);
                    if (testingCenterTimeSlots.checkSeatAvailable()) {
                        testingCenterTimeSlots.assignSeat(apptIter);
                        return true;
                    }
                    i++;
                }
                return false;
            } else {
                return false;
            }
        } catch (HibernateException he) {
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            session.close();
        }
        return true;
    }

    @Override
    public List<Appointment> findAllAppointmentsByExamId(String examId) {
        Session session = SessionManager.getInstance().openSession();
        Transaction tx = null;
        List<Appointment> appointments = new ArrayList<>();
        try {
            tx = session.beginTransaction();
            Query query = session.createQuery("from Appointment a where a.examId = :id");
            query.setParameter("id", examId);
            // If query won't get any record from table, the result will be an empty list.
            appointments = query.list();
            tx.commit();
        } catch (HibernateException he) {
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            session.close();
        }
        return appointments;
    }


    @Override
    public boolean checkDuplicateExam(String netid, String examId) {
        Session session = SessionManager.getInstance().openSession();
        Transaction tx = null;
        Appointment appt = null;
        try {
            tx = session.beginTransaction();
            Query query = session.createQuery("from Appointment a where a.examId = :examId and a.studentId = :studentId");
            query.setParameter("examId", examId);
            query.setParameter("studentId", netid);
            // If query won't get any record from table, the result will be an empty list.
            appt = (Appointment) query.uniqueResult();
            tx.commit();
        } catch (HibernateException he) {
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            session.close();
        }
        if (appt == null) {
            return false;
        } else {
            return true;   // Found Duplicate Appointment
        }
    }

    @Override
    public boolean checkOverlap(Appointment newAppointment) {
        Session session = SessionManager.getInstance().openSession();
        Transaction tx = null;
        List<Appointment> apptList = new ArrayList<>();
        boolean isOverlap = false;
        try {
            tx = session.beginTransaction();
            Query query = session.createQuery
                    ("FROM Appointment A WHERE A.studentId = :stuId");
            query.setParameter("stuId", newAppointment.getStudentId());
            apptList = query.list();     // Get all his appointment
            if(apptList == null || apptList.isEmpty()){
            }else {
                for (Appointment appt: apptList){
                    LocalDateTime startA = newAppointment.getStartDateTime();
                    LocalDateTime endA = newAppointment.getEndDateTime();
                    LocalDateTime startB = appt.getStartDateTime();
                    LocalDateTime endB = appt.getStartDateTime();

                    // (StartA <= EndB) and (EndA >= StartB)
                    if((!startA.isAfter(endB)) && (!endA.isAfter(startB))){
                        isOverlap = true;       // Overlap
                        break;
                    }
                }
            }
            tx.commit();
        } catch (HibernateException he) {
            if (tx != null) {
                tx.rollback();
            }
        } finally {
            session.close();
        }
        return isOverlap;
    }
}
