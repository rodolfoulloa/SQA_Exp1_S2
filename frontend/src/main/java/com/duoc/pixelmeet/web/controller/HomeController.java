package com.duoc.pixelmeet.web.controller;

import com.duoc.pixelmeet.web.client.ApiDtos.Home;
import com.duoc.pixelmeet.web.client.ApiException;
import com.duoc.pixelmeet.web.client.PixelMeetApiClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    private final PixelMeetApiClient api;

    public HomeController(PixelMeetApiClient api) {
        this.api = api;
    }

    /** [Publica] Eventos recientes y populares, banners comerciales y acceso al login. */
    @GetMapping({"/", "/home"})
    public String home(Model model) {
        Home home;
        try {
            home = api.home();
        } catch (ApiException ex) {
            home = new Home(List.of(), List.of(), List.of());
            model.addAttribute("avisoServicio", true);
        }
        model.addAttribute("recientes", home.recientes());
        model.addAttribute("populares", home.populares());
        model.addAttribute("banners", home.banners());
        return "home";
    }
}
