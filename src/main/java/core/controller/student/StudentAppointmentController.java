package core.controller.student;

import core.event.dao.AppointmentDao;
import core.user.dao.StudentDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/student/make-appointment")
public class StudentAppointmentController {
    @Autowired
    StudentDao studentDao;
    @Autowired
    AppointmentDao appointmentDao;

    @RequestMapping(value = "/makeAppointment", method = RequestMethod.POST)
    public ModelAndView makeAppointment(){
        ModelAndView model =  new ModelAndView("student/make-appointment");

    }

}