package com.evecodeideas.com.springboot.app.controllers;

import com.evecodeideas.com.springboot.app.controllers.util.pagenator.PageRender;
import com.evecodeideas.com.springboot.app.models.dao.IClienteDao;
import com.evecodeideas.com.springboot.app.models.entity.Cliente;
import com.evecodeideas.com.springboot.app.models.service.IClienteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;


@Controller
@SessionAttributes("cliente")
public class ClienteController {

    @Autowired
//    private IClienteDao clienteDao;
    private IClienteService clienteService;
    private RedirectAttributes flash;
    private final Logger log = LoggerFactory.getLogger(getClass());

    @GetMapping(value = "/ver/{id}")
    public String ver (@PathVariable(value = "id") Long id, Map<String, Object> model, RedirectAttributes flash){

        Cliente cliente = clienteService.findOne(id);
        if(cliente == null){
            flash.addFlashAttribute("error", "There is not exist the client with id " + id);
            return "redirect:/listar";
        }
        model.put("cliente", cliente);
        model.put("titulo", "Client's Details: " + cliente.getNombre());
        return "ver";
    }

    @GetMapping("/listar")
    public String listar(@RequestParam(name="page",defaultValue = "0") int page, Model model){

        Pageable pageRequest = PageRequest.of(page, 4);
        Page<Cliente> clientes = clienteService.findAll(pageRequest);
        PageRender<Cliente> pageRender = new PageRender<>("/listar", clientes);
        model.addAttribute("titulo","Listado de clientes");
        model.addAttribute("clientes", clientes);
        model.addAttribute("page", pageRender);

        return "listar";
    }

    @GetMapping("/form")
    public String crear(Map<String, Object> model){

        Cliente cliente = new Cliente();
        model.put("cliente", cliente);
        model.put("titulo", "Formulario");

        return "form";
    }

    @PostMapping("/form")
    //@Valid Cliente cliente, BindingResult result SIEMPRE TIENE QUE IR JUNTOS
    //BindingResult para mostrar el error
    public String guardar(@Valid Cliente cliente, BindingResult result, Model model, @RequestParam("file") MultipartFile photo, RedirectAttributes flash, SessionStatus status){

        if(result.hasErrors()){
            model.addAttribute("titulo", "Formulario");
            return "form";
        }
        if(!photo.isEmpty()){
//            Path resourcesDirectory = Paths.get("src//main//resources//static/uploads"); //Para guardar en la carpeta del proyecto
//            String rootPath = "C://Temp//uploads"; //Para guardar en C
            String uniqueFileName = UUID.randomUUID().toString() + "_" + photo.getOriginalFilename();
            Path rootPath = Paths.get("uploads").resolve(uniqueFileName);
            Path rootAbsolutePath = rootPath.toAbsolutePath();

            log.info("rootPath:" + rootPath);
            log.info("rootAbsolutePath:" + rootAbsolutePath);

            try {
//                byte[] bytes = photo.getBytes();
//                Path completPath = Paths.get(rootPath + "//" +  photo.getOriginalFilename());
//                Files.write(completPath, bytes);
                // we can replace the three above lines by:
                Files.copy(photo.getInputStream(), rootAbsolutePath);
                flash.addFlashAttribute("info", "You had uploaded successfully '" + uniqueFileName + "'" );
                cliente.setPhoto(uniqueFileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String mensajeFlash = (cliente.getId() != null)? "Cliente editado con exito" : "Creado con exito";

        clienteService.save(cliente);
        status.setComplete();
        flash.addFlashAttribute("success", mensajeFlash);
        return "redirect:listar";
    }

    @GetMapping("/form/{id}")
    public String edita(@PathVariable(value="id") Long id, Map<String, Object> model, RedirectAttributes flash){

        Cliente cliente = null;

        if(id > 0){
            cliente = clienteService.findOne(id);
            if(cliente == null){
                flash.addFlashAttribute("error", "El id del cliente no existe en la base de datos");
                return "redirect:/listar";
            }
        }else{
            flash.addFlashAttribute("error", "El id del cliente no puede ser cero");
            return "redirect:/listar";
        }

        model.put("cliente", cliente);
        model.put("titulo", "Editar Cliente");

        return "form";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable(value="id") Long id, RedirectAttributes flash){

        if(id > 0){
            clienteService.delete(id);
            flash.addFlashAttribute("success", "Cliente eliminado con exito");
        }
        return "redirect:/listar";
    }

}
