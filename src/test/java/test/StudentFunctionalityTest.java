//package test;
//
//import core.event.Appointment;
//import core.event.Term;
//import core.event.TestingCenterInfo;
//import core.event.dao.AppointmentDaoImp;
//import core.event.Exam;
//import core.service.SessionManager;
//import core.user.Student;
//import org.apache.log4j.Logger;
//import org.hibernate.HibernateException;
//import org.hibernate.Query;
//import org.hibernate.Session;
//import org.hibernate.Transaction;
//
//import java.time.LocalDateTime;
//import java.util.Iterator;
//import java.util.List;
//
//public class StudentFunctionalityTest {
//
//    public static final Logger log = Logger.getLogger(Log4J.class);
//
//    public static void main(String[] args){
//
//        StudentFunctionalityTest sf = new StudentFunctionalityTest();
//
//        Exam exam = new Exam("cse_308","course", LocalDateTime.of(2015,10,29,0,0),LocalDateTime.of(2015,10,29, 5,0), 5,50,80,"aaa");
//        Exam exam2 = new Exam("cse_305","course", LocalDateTime.of(2015,6,20,15,10),LocalDateTime.of(2015,10,1,17,40), 2.5,50,80,"aaa");
//        Exam exam3 = new Exam("cse_305_1","course", LocalDateTime.of(2015,8,20,15,10),LocalDateTime.of(2015,9,16,17,40), 2.5,50,80,"aaa");
//        Exam exam4 = new Exam("mat_200","course", LocalDateTime.of(2015,9,29,15,10),LocalDateTime.of(2015,9,30,5,40), 2.5,50,80,"aaa");
//        sf.addExam(exam);
//        sf.addExam(exam2);
//        sf.addExam(exam3);
//        sf.addExam(exam4);
//
//        Student student1 = new Student("balabala", "abc", "zeqing", "li", "l.caecar@gmail.com");
//        sf.addStudent(student1);
//
//        Appointment appt1 = new Appointment("cse308examZeqli", "cse_308", "admin", LocalDateTime.of(2015, 10, 29, 1, 0),
//                    LocalDateTime.of(2015,10,29,2,30),"balabala", "5R11", false);
//        sf.makeAppointment(appt1);
//
//        Appointment appt2 = new Appointment("cse308examZeqliFail", "cse_308", "admin", LocalDateTime.of(2015, 8, 1, 1, 0),
//                LocalDateTime.of(2015,8,3,2,20),"balabala", "5R21", false);
//        sf.makeAppointment(appt2);
//
//        Appointment appt3 = new Appointment("cse305examZeqliSuccess", "cse_305", "admin", LocalDateTime.of(2015, 9, 29, 1, 0),
//                LocalDateTime.of(2015,9,30,2,20),"balabala", "5R12", false);
//        sf.makeAppointment(appt3);
//
//        Appointment appt4 = new Appointment("cse305examZeqliSuccess", "cse_305_1", "admin", LocalDateTime.of(2015, 8, 28, 1, 0),
//                LocalDateTime.of(2015,9,15,2,20),"balabala", "5R13", false);
//        sf.makeAppointment(appt4);
//
//        Appointment appt5 = new Appointment("mat200examFail", "mat_200", "admin", LocalDateTime.of(2015, 9, 29, 1, 0),
//                LocalDateTime.of(2015,9,30,2,20),"balabala", "5R14", false);
//        sf.makeAppointment(appt5);
//
//        sf.cancelAppointment(appt1);
//    }
//
//    public void makeAppointment(Appointment appt){
//        int[] data = getGapByAppt(appt);
//        /**
//         * @data[0]: int gap
//         * @data[1]: int setAsideSeat
//         */
//        if(checkLegalAppointment(appt, data[0], data[1])==true){
//            addAppointment(appt);
//            System.out.print("\nSuccess.");
//        }
//        else
//            System.out.print("\nFail.");
//        System.out.print("\n"+appt.getAppointmentID());
//    }
//
//    public int[] getGapByAppt(Appointment a) {
//        Term term = a.getTerm();
//        int[] data = new int[2];
//        Session session = SessionManager.getInstance().getOpenSession();
//        Transaction tx = null;
//        try {
//            tx = session.beginTransaction();
//            //map to object
//                String hql = "FROM TestingCenterInfo t WHERE t.term = :termId";
//                Term termId = term;
//                Query query = session.createQuery(hql);
//                query.setParameter("termId", "%" + term + "%");
//                List<TestingCenterInfo> list = query.list();
//                TestingCenterInfo tInfo = list.get(0);
//                data[0] = tInfo.getGap();
//                data[1] = tInfo.getNumSetAsideSeats();
//        }catch (HibernateException he) {
//            if(tx != null) {
//                tx.rollback();
//            }
//            log.error("", he);
//        } finally {
//            session.close();
//        }
//        return data;
//    }
//
//    // Insert a row into Appointment Table
//    public void addAppointment(Appointment appt) {
//        Session session = SessionManager.getInstance().openSession();
//        Transaction tx = null;
//        try {
//            tx = session.beginTransaction();
//            session.save(appt);
//            log.info("---------- addAppointmentForStudent(Appointment appt) ----------");
//            log.info("|  -AppointmentID" + appt.getAppointmentID());
//            log.info("|  -EmailId" + appt.getExamId());
//            log.info("|  -StartDateTime" + appt.getStartDateTime());
//            log.info("|  -EndDateTime" + appt.getEndDateTime());
//            log.info("|  -StudentId" + appt.getStudentId());
//            log.info("|  -Seat" + appt.getSeat());
//            log.info("|  -IsAttended" + appt.isAttend());
//            tx.commit();
//        } catch (HibernateException he) {
//            log.error("", he);
//            if (tx != null) {
//                tx.rollback();
//            }
//        } finally {
//            session.close();
//        }
//
//    }
////
//    public void addStudent(Student student){
//        Session session = SessionManager.getInstance().openSession();
//        Transaction tx = null;
//        try {
//            tx = session.beginTransaction();
//            session.save(student);
//            tx.commit();
//        }catch (HibernateException he){
//            he.printStackTrace();
//            if(tx != null){
//                tx.rollback();
//            }
//        }finally {
//            session.close();
//        }
//    }
//
//    public void addExam(Exam exam){
//        Session session = SessionManager.getInstance().openSession();
//        Transaction tx = null;
//        try {
//            tx = session.beginTransaction();
//            session.save(exam);
//            tx.commit();
//        }catch (HibernateException he){
//            log.error("", he);
//            if(tx != null){
//                tx.rollback();
//            }
//        }finally {
//            session.close();
//        }
//    }
//
//    public boolean checkLegalAppointment(Appointment a, int gap, int setAsideSeat){
//        Session session = SessionManager.getInstance().getOpenSession();
//        String studentIdCheck = a.getStudentId();
//        String examIdCheck = a.getExamId();
//        boolean result = true;
////        Session session = SessionManager.getInstance().getOpenSession();
//        Transaction tx = null;
//        try {
//            tx = session.beginTransaction();
//            Exam exam = (Exam)session.get(Exam.class, examIdCheck);
//            //the appt time is during exam time period
//            //check d
//            if((exam.getStartDateTime().isBefore(a.getStartDateTime()))
//                    &&(exam.getEndDateTime().isAfter(a.getEndDateTime()))) {
//                //map to object
//                String hql = "FROM Appointment b WHERE b.studentId = :stuId";
//                String stuId = studentIdCheck;
//                Query query = session.createQuery(hql);
//                query.setParameter("stuId", "%" + stuId + "%");
//                List<Appointment> list = query.list();
//                Iterator it = list.iterator();
//                while (it.hasNext()) {
//                    Appointment appt = new Appointment();
//                    appt = (Appointment) it.next();
//                    //check b
//                    if (!appt.getExamId().equals(examIdCheck)) {
//                        //check c and gap time
//                        if ( ((a.getStartDateTime().minusMinutes(gap)).isAfter(appt.getEndDateTime())) ||
//                                (a.getEndDateTime().isBefore((appt.getStartDateTime().minusMinutes(gap)))) ) {
//                            //check set aside seats
//
//                        } else {
//                            result = false;
//                            break;
//                        }
//                    } else {
//                        result = false;
//                        break;
//                    }
//                }
//            }
//            else{
//                result = false;
//            }
//        }catch (HibernateException he) {
//            if(tx != null) {
//                tx.rollback();
//            }
//            log.error("", he);
//        } finally {
//            session.close();
//        }
//        return result;
//    }
//
//    public void cancelAppointment(Appointment appt){
//        AppointmentDaoImp imp = new AppointmentDaoImp();
////        if(appt.getStartDateTime().minusHours(24).isAfter(LocalDateTime.now())){
//            imp.deleteAppointment(appt.getAppointmentID());
////            System.out.print("Successfully deleted.");
////        }
//        log.info("---------- cancelAppointment(Appointment appt) ----------");
//        log.info("|  -AppointmentID" + appt.getAppointmentID());
//        log.info("|  -EmailId" + appt.getExamId());
//        log.info("|  -StartDateTime" + appt.getStartDateTime());
//        log.info("|  -EndDateTime" + appt.getEndDateTime());
//        log.info("|  -StudentId" + appt.getStudentId());
//        log.info("|  -Seat" + appt.getSeat());
//        log.info("|  -IsAttended" + appt.isAttend());
//    }
//}