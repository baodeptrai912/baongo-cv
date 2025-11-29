package com.example.baoNgoCv.jpa.projection.company;


public record JobStatisticsProjection(
        Integer totalJobs,
        Integer openJobs,
        Integer closedJobs,
        Integer totalApplications,
        Integer expiringSoonCount
) {}
