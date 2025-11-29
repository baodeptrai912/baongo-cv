package com.example.baoNgoCv.model.dto.company;

import lombok.Data;

@Data
public class GetJobpostingManagingRequest {
    private int page = 0;
    private int size = 10;
}
