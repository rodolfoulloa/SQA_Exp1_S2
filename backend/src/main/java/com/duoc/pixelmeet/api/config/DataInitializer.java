package com.duoc.pixelmeet.api.config;

import com.duoc.pixelmeet.api.model.Banner;
import com.duoc.pixelmeet.api.model.Evento;
import com.duoc.pixelmeet.api.model.Modalidad;
import com.duoc.pixelmeet.api.model.Rol;
import com.duoc.pixelmeet.api.model.Usuario;
import com.duoc.pixelmeet.api.repository.BannerRepository;
import com.duoc.pixelmeet.api.repository.EventoRepository;
import com.duoc.pixelmeet.api.repository.RolRepository;
import com.duoc.pixelmeet.api.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Carga los datos iniciales solo si las tablas estan vacias: roles, 3 usuarios de prueba,
 * eventos y banners. Las contrasenas se guardan con BCrypt y pueden cambiarse por variables de entorno.
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RolRepository roles;
    private final UsuarioRepository usuarios;
    private final EventoRepository eventos;
    private final BannerRepository banners;
    private final PasswordEncoder encoder;
    private final String adminPassword;
    private final String gamerPassword;

    public DataInitializer(RolRepository roles, UsuarioRepository usuarios, EventoRepository eventos,
                           BannerRepository banners, PasswordEncoder encoder,
                           @Value("${app.seed.admin-password:Admin123!}") String adminPassword,
                           @Value("${app.seed.gamer-password:Gamer123!}") String gamerPassword) {
        this.roles = roles;
        this.usuarios = usuarios;
        this.eventos = eventos;
        this.banners = banners;
        this.encoder = encoder;
        this.adminPassword = adminPassword;
        this.gamerPassword = gamerPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Rol user = roles.findByNombre(Rol.USER).orElseGet(() -> roles.save(new Rol(Rol.USER)));
        Rol admin = roles.findByNombre(Rol.ADMIN).orElseGet(() -> roles.save(new Rol(Rol.ADMIN)));

        if (usuarios.count() == 0) {
            Usuario a = new Usuario("admin", "admin@pixelmeet.cl", encoder.encode(adminPassword),
                    "Administrador PixelMeet", "admin");
            a.agregarRol(user);
            a.agregarRol(admin);
            a.setAvatar("avatar-4");

            Usuario g1 = new Usuario("gamer1", "gamer1@pixelmeet.cl", encoder.encode(gamerPassword),
                    "Camila Rojas", "camirojas");
            g1.agregarRol(user);
            g1.setAvatar("avatar-2");
            g1.setJuegos(List.of("Valorant", "League of Legends"));

            Usuario g2 = new Usuario("gamer2", "gamer2@pixelmeet.cl", encoder.encode(gamerPassword),
                    "Diego Muñoz", "diegomz");
            g2.agregarRol(user);
            g2.setAvatar("avatar-3");
            g2.setJuegos(List.of("Street Fighter 6", "Tekken 8"));

            usuarios.saveAll(List.of(a, g1, g2));
            log.info("Usuarios iniciales creados: admin, gamer1, gamer2");
        }

        if (eventos.count() == 0) {
            eventos.saveAll(eventosIniciales());
            log.info("Eventos iniciales creados: {}", eventos.count());
        }

        if (banners.count() == 0) {
            banners.saveAll(List.of(
                    new Banner("NeoKeys", "Teclados mecánicos con 15% de descuento para la comunidad PixelMeet.",
                            "Ver teclados", "banner-neokeys.svg", 1),
                    new Banner("ByteNet Fibra", "Plan gamer 1 Gbps con ping bajo a servidores de Santiago.",
                            "Cotizar plan", "banner-bytenet.svg", 2),
                    new Banner("Respawn Café", "Café, snacks y consolas libres. Muestra tu entrada y obtén 2x1.",
                            "Cómo llegar", "banner-respawn.svg", 3)));
        }
    }

    private static LocalDateTime f(int anio, int mes, int dia, int hora, int min) {
        return LocalDateTime.of(anio, mes, dia, hora, min);
    }

    private static LocalDateTime hace(int dias) {
        return LocalDateTime.now().minusDays(dias);
    }

    private List<Evento> eventosIniciales() {
        return List.of(
            new Evento("Copa Andina de Valorant", "Valorant", "Torneo", Modalidad.PRESENCIAL, "Chile", "Santiago",
                    f(2026, 11, 21, 10, 0),
                    "Torneo 5v5 con equipos de todo Chile y premio de $3.000.000.",
                    "La Copa Andina reúne a los mejores equipos amateur del país en un formato de doble eliminación. "
                            + "Habrá casteo en vivo, zona de streaming y firma de autógrafos con jugadores profesionales.",
                    "Andes Esports",
                    "Equipos de 5 jugadores mayores de 16 años. Cuenta Riot con rango mínimo Oro. "
                            + "Llevar periféricos propios; los PC los pone la organización.",
                    "Inscripción por equipo hasta el 15 de noviembre en el sitio de Andes Esports. "
                            + "Cupos limitados a 32 equipos.",
                    "$20.000 por equipo")
                    .ubicacion("Movistar Arena, Av. Beaucheff 1204, Santiago").cupos(32).interesados(1840)
                    .destacado(true).publicadoEn(hace(2))
                    .fotos("valorant-1.svg", "valorant-2.svg", "estadio-1.svg"),

            new Evento("LAN Party Valparaíso 2026", "Counter-Strike 2", "LAN Party", Modalidad.PRESENCIAL, "Chile",
                    "Valparaíso", f(2026, 11, 28, 9, 0),
                    "48 horas de LAN con torneos de CS2, Rocket League y retro.",
                    "La LAN más grande de la Quinta Región vuelve con 300 puestos, red de 10 Gbps, "
                            + "zona de descanso y torneos con premios en hardware.",
                    "Club Gamer Porteño",
                    "Traer PC o notebook, cable de red y audífonos. Mayores de 14 años "
                            + "(menores de 18 con autorización firmada).",
                    "Venta de entradas en línea; el puesto se asigna por orden de llegada al confirmar el pago.",
                    "$15.000")
                    .ubicacion("Centro de Eventos Duoc UC, Av. Brasil 2021, Valparaíso").cupos(300).interesados(960)
                    .publicadoEn(hace(5)).fotos("lan-1.svg", "lan-2.svg", "cs2-1.svg"),

            new Evento("Clasificatorio online League of Legends Sur", "League of Legends", "Clasificatorio",
                    Modalidad.ONLINE, "Chile", "Concepción", f(2026, 12, 5, 18, 0),
                    "Clasificatorio abierto para la liga regional LoL Sur.",
                    "Fase de grupos online los fines de semana de diciembre. Los 4 mejores equipos clasifican a la "
                            + "final presencial de enero en Concepción.",
                    "Liga Sur Gaming",
                    "Equipos de 5 titulares y 1 suplente, servidor LAS, nivel de invocador 30 o superior.",
                    "Registro del equipo en el formulario de Liga Sur Gaming; se envía el enlace de Discord al "
                            + "confirmar.",
                    "Gratis")
                    .enlaceAcceso("https://discord.gg/ligasur-ejemplo").cupos(128).interesados(2210)
                    .destacado(true).publicadoEn(hace(1)).fotos("lol-1.svg", "lol-2.svg"),

            new Evento("Smash Fest Santiago", "Super Smash Bros.", "Torneo", Modalidad.PRESENCIAL, "Chile",
                    "Santiago", f(2026, 12, 12, 11, 0),
                    "Torneo 1v1 de Smash Ultimate con side events de dobles.",
                    "Formato doble eliminación al mejor de 3 (top 8 al mejor de 5). Habrá zona libre para jugar "
                            + "casual y venta de comida.",
                    "Smash Chile Comunidad",
                    "Traer control propio. Reglamento competitivo oficial de la comunidad.",
                    "Inscripción en start.gg hasta el 10 de diciembre o el mismo día si quedan cupos.",
                    "$8.000")
                    .ubicacion("Centro Cultural GAM, Av. Libertador B. O'Higgins 227, Santiago").cupos(256)
                    .interesados(640).publicadoEn(hace(8)).fotos("smash-1.svg", "torneo-1.svg"),

            new Evento("Meetup Minecraft Builders", "Minecraft", "Meetup", Modalidad.ONLINE, "Argentina",
                    "Buenos Aires", f(2026, 12, 13, 17, 0),
                    "Encuentro online para mostrar construcciones y aprender redstone.",
                    "Charlas cortas de constructores latinoamericanos, concurso de construcción por equipos y "
                            + "sorteo de cuentas premium.",
                    "Builders LATAM",
                    "Minecraft Java 1.21 o superior. Micrófono opcional.",
                    "Inscripción gratuita en el formulario del evento; la IP del servidor se envía por correo.",
                    "Gratis")
                    .enlaceAcceso("https://twitch.tv/builderslatam-ejemplo").interesados(410)
                    .publicadoEn(hace(3)).fotos("minecraft-1.svg", "minecraft-2.svg"),

            new Evento("Fighting Games Night", "Street Fighter 6", "Torneo", Modalidad.PRESENCIAL, "Chile",
                    "Viña del Mar", f(2026, 12, 19, 19, 0),
                    "Torneo de SF6 y Tekken 8 con premios en dinero.",
                    "Noche de juegos de pelea con torneo principal de Street Fighter 6 y side de Tekken 8. "
                            + "Transmisión en vivo con comentaristas.",
                    "FGC Quinta",
                    "Traer control o arcade stick. Mayores de 15 años.",
                    "Inscripción presencial hasta las 18:30 hrs o por adelantado en el Instagram del organizador.",
                    "$5.000")
                    .ubicacion("Respawn Café, 5 Norte 650, Viña del Mar").cupos(64).interesados(380)
                    .publicadoEn(hace(12)).fotos("sf6-1.svg", "torneo-1.svg"),

            new Evento("GamerFest Lima", "Varios", "Convención", Modalidad.PRESENCIAL, "Perú", "Lima",
                    f(2027, 1, 16, 10, 0),
                    "Convención gamer con lanzamientos, cosplay y zona indie.",
                    "Dos días de actividades: stands de marcas, zona de juegos indie latinoamericanos, concurso "
                            + "de cosplay y paneles con desarrolladores.",
                    "GamerFest Perú",
                    "Entrada general para mayores de 12 años; menores acompañados por un adulto.",
                    "Entradas en la boletería oficial del evento; descuento para grupos de 4 o más.",
                    "S/ 45")
                    .ubicacion("Centro de Exposiciones Jockey, Lima").interesados(3150).destacado(true)
                    .publicadoEn(hace(20)).fotos("convencion-1.svg", "cosplay-1.svg", "estadio-1.svg"),

            new Evento("Rocket League 2v2 Online Cup", "Rocket League", "Torneo", Modalidad.ONLINE, "México",
                    "Ciudad de México", f(2027, 1, 23, 16, 0),
                    "Copa online 2v2 abierta a toda Latinoamérica.",
                    "Formato suizo seguido de playoffs. Las finales se transmiten con casteo en español.",
                    "RL Latam League",
                    "Equipos de 2 jugadores, servidores SAM o NA Este, cuenta Epic vinculada.",
                    "Registro del equipo en el bracket online hasta 24 horas antes del inicio.",
                    "Gratis")
                    .enlaceAcceso("https://youtube.com/@rllatam-ejemplo").cupos(256).interesados(720)
                    .publicadoEn(hace(4)).fotos("rocket-1.svg", "torneo-1.svg"),

            new Evento("Fortnite Creative Jam", "Fortnite", "Game Jam", Modalidad.ONLINE, "Chile", "Santiago",
                    f(2027, 2, 6, 10, 0),
                    "48 horas para crear un mapa en Fortnite Creative / UEFN.",
                    "Game jam para creadores de mapas. Un jurado evalúa jugabilidad, originalidad y tema; "
                            + "los mapas ganadores se destacan en la comunidad.",
                    "Creadores Chile",
                    "Equipos de 1 a 3 personas, cuenta Epic y UEFN instalado.",
                    "Inscripción en el formulario de Creadores Chile; el tema se revela al iniciar.",
                    "Gratis")
                    .enlaceAcceso("https://discord.gg/creadoreschile-ejemplo").interesados(290)
                    .publicadoEn(hace(0)).fotos("fortnite-1.svg", "lan-2.svg"),

            new Evento("Pokémon VGC Regional Concepción", "Pokémon", "Torneo", Modalidad.PRESENCIAL, "Chile",
                    "Concepción", f(2027, 2, 20, 10, 0),
                    "Regional de Pokémon Escarlata y Púrpura formato VGC.",
                    "Torneo por suizo con corte a top 8. Incluye categorías Junior, Senior y Master.",
                    "Liga Pokémon Biobío",
                    "Traer consola Nintendo Switch con el equipo registrado y Player ID.",
                    "Inscripción previa obligatoria en la plataforma de la liga.",
                    "$10.000")
                    .ubicacion("Mall Plaza Trébol, Av. Jorge Alessandri 3177, Talcahuano").cupos(128)
                    .interesados(530).publicadoEn(hace(6)).fotos("pokemon-1.svg", "torneo-1.svg"),

            new Evento("EA FC Liga Universitaria", "EA Sports FC", "Liga", Modalidad.PRESENCIAL, "Chile",
                    "Santiago", f(2027, 3, 13, 15, 0),
                    "Fecha inaugural de la liga universitaria de EA Sports FC.",
                    "Representantes de distintas casas de estudio compiten en formato liga. "
                            + "Esta fecha incluye la presentación de los equipos.",
                    "Deporte Electrónico Universitario",
                    "Ser alumno regular con credencial vigente. Jugar en PlayStation 5.",
                    "Inscripción a través de la dirección de deportes de cada institución.",
                    "Gratis")
                    .ubicacion("Duoc UC Sede San Joaquín, Av. Vicuña Mackenna 4917, Santiago").cupos(40)
                    .interesados(860).publicadoEn(hace(9)).fotos("fc-1.svg", "estadio-1.svg"),

            new Evento("Tekken 8 Buenos Aires Showdown", "Tekken 8", "Torneo", Modalidad.PRESENCIAL, "Argentina",
                    "Buenos Aires", f(2027, 4, 10, 12, 0),
                    "Torneo internacional de Tekken 8 con invitados de la región.",
                    "Torneo abierto con invitados de Chile, Perú y Brasil. Incluye exhibiciones y side events.",
                    "Tekken Argentina",
                    "Traer control. Inscripción individual. Mayores de 16 años.",
                    "Inscripción en start.gg; cierre una semana antes del evento.",
                    "ARS 25.000")
                    .ubicacion("Centro Costa Salguero, Buenos Aires").cupos(512).interesados(1210)
                    .publicadoEn(hace(15)).fotos("tekken-1.svg", "torneo-1.svg", "estadio-1.svg")
        );
    }
}
