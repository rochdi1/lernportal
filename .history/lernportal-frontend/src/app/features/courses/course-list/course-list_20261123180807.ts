import { Component, OnInit } from '@angular/core';
import { Course } from '../../../shared/models/course.model';
import { CourseService } from '../../../core/services/course.service';
import { CommonModule } from '@angular/common';

@Component({
  imports: [CommonModule],
  selector: 'app-course-list',
  styleUrl: './course-list.scss',
  templateUrl: './course-list.html',
})
export class CourseList implements OnInit {
  courses: Course[] = [];

  constructor(private courseService: CourseService) {}

  ngOnInit(): void {
    this.courseService.getAll().subscribe(data => this.courses = data);
  }
}