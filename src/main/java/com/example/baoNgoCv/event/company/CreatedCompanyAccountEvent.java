package com.example.baoNgoCv.event.company;

/**
 * Event published when a new company account is successfully created and saved.
 *
 * @param companyId    The ID of the newly created company.
 * @param companyName  The name of the company.
 * @param contactEmail The contact email of the company.
 */
public record CreatedCompanyAccountEvent(Long companyId, String companyName, String contactEmail) {
}