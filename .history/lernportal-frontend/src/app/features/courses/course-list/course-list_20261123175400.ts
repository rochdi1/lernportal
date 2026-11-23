import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CourseService } from '../../../core/services/course.service';
import { Course } from '../../../shared/models/course.model';
@Component({
  imports: [CommonModule],
  selector: 'app-course-list',
  styleUrl: './course-list.scss',
  templateUrl: './course-list.html',
})
export class CourseList {}
