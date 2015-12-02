package core.controller.student;

import core.event.Appointment;
import core.event.TestingCenterTimeSlots;
import core.event.dao.AppointmentDao;
import core.event.dao.ExamDao;
import core.event.dao.TestingCenterTimeSlotsDao;
import core.helper.StringResources;
import core.user.SessionProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/student/make-appointment")
public class StudentAppointmentController {
    @Autowired
    private AppointmentDao appointmentDao;
    @Autowired
    private TestingCenterTimeSlotsDao timeSlotsDao;
    @Autowired
    private ExamDao examDao;

    @RequestMapping("new/{id}")
    public ModelAndView makeAppointment(@PathVariable("id") String examId,
                                        ModelAndView model){
        model.setViewName("select-appointment");
        model.addObject("exam", examDao.findByExamId(examId).getExamName());
        model.addObject("heading", StringResources.STUDENT_MAKE_APPOINTMENT);
        model.addObject("timeSlots", timeSlotsDao.findAllTimeSlotsByExamId(examId));
        return model;
    }

    @RequestMapping(value = "submit", method = RequestMethod.POST)
    public ModelAndView selectAppointment(@ModelAttribute Appointment appointment,
                                          HttpSession session,
                                          ModelAndView model) {
        SessionProfile profile = (SessionProfile) session.getAttribute("sessionUser");
        model.setViewName("redirect:/student/view-appointments");
        model.addObject("heading", StringResources.STUDENT_VIEW_APPOINTMENTS);
        model.addObject("errorMessage", "Appointment submitted.");
        TestingCenterTimeSlots slot
                = timeSlotsDao.findTimeSlotById(appointment.getSlotId());
        appointment.setExamId(slot.getExamId());
        appointment.setStartDateTime(slot.getBegin());
        appointment.setEndDateTime(slot.getEnd());
        appointment.setStudentId(profile.getUserId());
        appointment.setExamName
                (examDao.findByExamId
                        (slot.getExamId()).getExamName());
        appointment.setTerm
                (examDao.findByExamId
                        (appointment.getExamId()).getTerm());

        appointmentDao.insertAppointment(appointment);
        return model;
    }
}