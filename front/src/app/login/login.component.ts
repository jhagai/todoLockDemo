import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {HttpClient} from '@angular/common/http';
import {finalize, tap} from 'rxjs/operators';
import {Router} from '@angular/router';
import {AuthService} from '../services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  formGroup: FormGroup;

  loading = false;

  constructor(private fb: FormBuilder,
              private router: Router,
              private http: HttpClient,
              private authService: AuthService) {
  }

  ngOnInit() {
    this.formGroup = this.fb.group({
      login: this.fb.control(''),
      password: this.fb.control('')
    });
  }

  submit() {
    const login = this.formGroup.controls['login'].value;
    const password = this.formGroup.controls['password'].value;

    this.loading = true;

    this.authService.authenticate(login, password)
      .pipe(
        tap(
          () => {
            this.router.navigate(['dashboard']);
          }
        ),
        finalize(() => {
          this.loading = false;
        })
      )
      .subscribe();
  }

}
