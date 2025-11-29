package com.example.baoNgoCv.service.utilityService;

import com.example.baoNgoCv.jpa.repository.CompanyRepository;
import com.example.baoNgoCv.jpa.repository.UserRepository;
import com.example.baoNgoCv.model.entity.Company;
import com.example.baoNgoCv.model.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("uiHelper") // Đặt tên bean là uiHelper để gọi trong HTML cho ngắn
public class UiHelperService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    // Hàm này nhận vào chính cái Principal (người đang đăng nhập)
    public String getLatestAvatar(Object principal) {

        // Trường hợp 1: Là User (người tìm việc)
        if (principal instanceof User) {
            Long userId = ((User) principal).getId();
            return userRepository.findById(userId)
                    .map(User::getProfilePicture)
                    .orElse(null); // hoặc trả về ảnh mặc định
        }

        // Trường hợp 2: Là Company (nhà tuyển dụng)
        if (principal instanceof Company) {
            Long companyId = ((Company) principal).getId();
            return companyRepository.findById(companyId)
                    .map(Company::getCompanyLogo)
                    .orElse(null);
        }

        return null; // Trường hợp không xác định
    }
}
