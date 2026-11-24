import { Component, OnInit, signal } from '@angular/core';
import { Course } from '../../../shared/models/course.model';
import { CourseService } from '../../../core/services/course.service';
import { CommonModule } from '@angular/common';

@Component({
  imports: [CommonModule],
  selector: 'app-course-list',
  standalone: true,
  styleUrl: './course-list.scss',
  templateUrl: './course-list.html',
})
export class CourseList implements OnInit {
  courses = signal<Course[]>([]);
  loading = signal(true);
  error = signal<string | null>(null);

  constructor(private courseService: CourseService) {}

  ngOnInit(): void {
    this.loadCourses();
  }

  loadCourses(): void {
    this.loading.set(true);
    this.error.set(null);

    this.courseService.getAll().subscribe({
      next: (data) => {
        this.courses.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Fehler beim Laden der Kurse:', err);
        this.error.set('Kurse konnten nicht geladen werden. Bitte Backend prüfen.');
        this.loading.set(false);
      },
    });
  }
}