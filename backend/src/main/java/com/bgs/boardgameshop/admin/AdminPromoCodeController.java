package com.bgs.boardgameshop.admin;

import com.bgs.boardgameshop.admin.dto.AdminPromoCodeRequest;
import com.bgs.boardgameshop.admin.dto.AdminPromoCodeResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/promo-codes")
public class AdminPromoCodeController {

    private final AdminPromoCodeService adminPromoCodeService;

    public AdminPromoCodeController(AdminPromoCodeService adminPromoCodeService) {
        this.adminPromoCodeService = adminPromoCodeService;
    }

    @GetMapping
    public List<AdminPromoCodeResponse> getPromoCodes() {
        return adminPromoCodeService.getPromoCodes();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminPromoCodeResponse createPromoCode(@Valid @RequestBody AdminPromoCodeRequest request) {
        return adminPromoCodeService.createPromoCode(request);
    }

    @PutMapping("/{id}")
    public AdminPromoCodeResponse updatePromoCode(@PathVariable Long id, @Valid @RequestBody AdminPromoCodeRequest request) {
        return adminPromoCodeService.updatePromoCode(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePromoCode(@PathVariable Long id) {
        adminPromoCodeService.deletePromoCode(id);
    }
}
