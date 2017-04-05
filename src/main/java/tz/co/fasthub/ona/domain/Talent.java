package tz.co.fasthub.ona.domain;


import org.springframework.social.oauth1.OAuthToken;

import javax.persistence.*;

/**
 * Created by root on 2/23/17.
 */
@Entity
public class Talent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long talent_id;
    private String fname;
    private String lname;
    private String email;
    private String password;
    private String Cpassword;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "twitterAccount_id")
    private TwitterTalentAccount twitterTalentAccount;

    public Talent(){}

    public Talent(String fname, String lname, String email, String password, OAuthToken accessToken) {
        this.fname = fname;
        this.lname = lname;
        this.email = email;
        this.password = password;}

    @Override
    public String toString() {
        return "Talent{" +
                "talent_id=" + talent_id +
                ", fname='" + fname + '\'' +
                ", lname='" + lname + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", Cpassword='" + Cpassword + '\'' +
                '}';
    }

    public Long getTalent_id() {
        return talent_id;
    }

    public void setTalent_id(Long talent_id) {
        this.talent_id = talent_id;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
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

    public String getCpassword() {
        return Cpassword;
    }

    public void setCpassword(String cpassword) {
        Cpassword = cpassword;
    }

    public TwitterTalentAccount getTwitterTalentAccount() {
        return twitterTalentAccount;
    }

    public void setTwitterTalentAccount(TwitterTalentAccount twitterTalentAccount) {
        this.twitterTalentAccount = twitterTalentAccount;
    }
}