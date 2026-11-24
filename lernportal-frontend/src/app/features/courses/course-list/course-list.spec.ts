import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CourseList } from './course-list';
import { CourseService } from '../../../core/services/course.service';
import { Observable, of, throwError } from 'rxjs';
import { Course } from '../../../shared/models/course.model';

describe('CourseList', () => {
  let component: CourseList;
  let fixture: ComponentFixture<CourseList>;
  let mockCourseService: CourseService;

  const mockCourses: Course[] = [
    { id: 1, title: 'Java', description: 'Einstieg' },
    { id: 2, title: 'TypeScript', description: 'Fortgeschritten' },
  ];

  beforeEach(async () => {
    mockCourseService = {
      getAll: vi.fn(),
      create: vi.fn(),
      update: vi.fn(),
      delete: vi.fn(),
    } as unknown as CourseService;

    await TestBed.configureTestingModule({
      imports: [CourseList],
      providers: [
        { provide: CourseService, useValue: mockCourseService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CourseList);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should show loading text initially', () => {
    // Default: loading is true, courses empty - mock getAll to return a never-completing observable
    (mockCourseService.getAll as ReturnType<typeof vi.fn>).mockReturnValue(
      new Observable(observer => {
        // Never call observer.next(), observer.complete(), or observer.error()
        // This simulates a pending request
      })
    );

    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Lade Kurse…');
  });

  it('should show error message when HTTP fails', () => {
    (mockCourseService.getAll as ReturnType<typeof vi.fn>).mockReturnValue(
      throwError(() => new Error('Network error'))
    );

    fixture = TestBed.createComponent(CourseList);
    component = fixture.componentInstance;
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Kurse konnten nicht geladen werden');
  });

  it('should render course list on success', () => {
    (mockCourseService.getAll as ReturnType<typeof vi.fn>).mockReturnValue(of(mockCourses));

    fixture = TestBed.createComponent(CourseList);
    component = fixture.componentInstance;
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Java');
    expect(compiled.textContent).toContain('TypeScript');
    expect(compiled.querySelectorAll('li').length).toBe(2);
  });

  it('should show empty message when no courses', () => {
    (mockCourseService.getAll as ReturnType<typeof vi.fn>).mockReturnValue(of([]));

    fixture = TestBed.createComponent(CourseList);
    component = fixture.componentInstance;
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Keine Kurse vorhanden');
  });
});
