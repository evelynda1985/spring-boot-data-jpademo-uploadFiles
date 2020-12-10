package com.evecodeideas.com.springboot.app.controllers;

import com.evecodeideas.com.springboot.app.controllers.util.pagenator.PageRender;
import com.evecodeideas.com.springboot.app.models.entity.Cliente;
import com.evecodeideas.com.springboot.app.models.service.IClienteService;
import com.evecodeideas.com.springboot.app.models.service.IUploadFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import javax.validation.Valid;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;


@Controller
@SessionAttributes("cliente")
public class ClienteController {

    @Autowired
    IUploadFileService uploadFileService;

    @Autowired
    private IClienteService clienteService;
    private RedirectAttributes flash;

    @GetMapping(value = "/uploads/{fileName:.+}")
//Expresion regular para que no se borre el archivo, pasara el nombre del archivo con la extension
    public ResponseEntity<Resource> VerFoto(@PathVariable String fileName) {

        Resource resource = null;
        try {
            resource = uploadFileService.load(fileName);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachement; fileName=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping(value = "/ver/{id}")
    public String ver(@PathVariable(value = "id") Long id, Map<String, Object> model, RedirectAttributes flash) {

        Cliente cliente = clienteService.findOne(id);
        if (cliente == null) {
            flash.addFlashAttribute("error", "There is not exist the client with id " + id);
            return "redirect:/listar";
        }
        model.put("cliente", cliente);
        model.put("titulo", "Client's Details: " + cliente.getNombre());
        return "ver";
    }

    @GetMapping("/listar")
    public String listar(@RequestParam(name = "page", defaultValue = "0") int page, Model model) {

        Pageable pageRequest = PageRequest.of(page, 4);
        Page<Cliente> clientes = clienteService.findAll(pageRequest);
        PageRender<Cliente> pageRender = new PageRender<>("/listar", clientes);
        model.addAttribute("titulo", "Listado de clientes");
        model.addAttribute("clientes", clientes);
        model.addAttribute("page", pageRender);

        return "listar";
    }

    @GetMapping("/form")
    public String crear(Map<String, Object> model) {

        Cliente cliente = new Cliente();
        model.put("cliente", cliente);
        model.put("titulo", "Formulario");

        return "form";
    }

    @PostMapping("/form")
    //@Valid Cliente cliente, BindingResult result SIEMPRE TIENE QUE IR JUNTOS
    //BindingResult para mostrar el error
    public String guardar(@Valid Cliente cliente, BindingResult result, Model model, @RequestParam("file") MultipartFile photo, RedirectAttributes flash, SessionStatus status) {

        if (result.hasErrors()) {
            model.addAttribute("titulo", "Formulario");
            return "form";
        }

        if (!photo.isEmpty()) {
            if (cliente.getId() != null
                    && cliente.getId() > 0
                    && cliente.getPhoto() != null
                    && cliente.getPhoto().length() > 0) {

                uploadFileService.delete(cliente.getPhoto());
            }
            String uniqueFileName = null;
            try {
                uniqueFileName = uploadFileService.copy(photo);
            } catch (IOException e) {
                e.printStackTrace();
            }
            flash.addFlashAttribute("info", "You had uploaded successfully '" + uniqueFileName + "'");
            cliente.setPhoto(uniqueFileName);
        }
        String mensajeFlash = (cliente.getId() != null) ? "Cliente editado con exito" : "Creado con exito";

        clienteService.save(cliente);
        status.setComplete();
        flash.addFlashAttribute("success", mensajeFlash);
        return "redirect:listar";
    }

    @GetMapping("/form/{id}")
    public String edita(@PathVariable(value = "id") Long id, Map<String, Object> model, RedirectAttributes flash) {

        Cliente cliente = null;

        if (id > 0) {
            cliente = clienteService.findOne(id);
            if (cliente == null) {
                flash.addFlashAttribute("error", "El id del cliente no existe en la base de datos");
                return "redirect:/listar";
            }
        } else {
            flash.addFlashAttribute("error", "El id del cliente no puede ser cero");
            return "redirect:/listar";
        }

        model.put("cliente", cliente);
        model.put("titulo", "Editar Cliente");

        return "form";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable(value = "id") Long id, RedirectAttributes flash) {

        if (id > 0) {
            Cliente cliente = clienteService.findOne(id);
            clienteService.delete(id);
            flash.addFlashAttribute("success", "Cliente eliminado con exito");

            if (uploadFileService.delete(cliente.getPhoto())) {
                flash.addFlashAttribute("info", "Foto " + cliente.getPhoto() + " Eliminada con exito");
            }
        }
        return "redirect:/listar";
    }

}
