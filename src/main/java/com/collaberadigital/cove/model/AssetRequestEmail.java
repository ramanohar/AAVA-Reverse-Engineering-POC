package com.collaberadigital.cove.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AssetRequestEmail {
    private String url;
    private String request_format;
    private String asset_name;
    private String email_address;
    private String request_id;

}
