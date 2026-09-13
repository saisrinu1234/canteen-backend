package com.example.canteen.login;

public class ResetPasswordRequest {

    private String email;
    private String password;

    public ResetPasswordRequest() {
    }

    public ResetPasswordRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getToken'");
    }
}