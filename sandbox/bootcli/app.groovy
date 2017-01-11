@Grab("spring-boot-starter-actuator")
@RestController
class app {

    @RequestMapping("/")
    String home() {
        System.getenv("HOSTNAME") + " "
    }

}