(ns ad.core-test
  (:use midje.sweet)
  (:require [ad.core :refer :all]))

(facts "Basic derivatives"

  ;; f(x) = x^2
  ;; f'(x)= 2*x
  (let [f (fn [x] (d* x x))
        f' (derivative-F f)]
    (f' 0) => 0
    (f' 2) => 4
    (f' 4) => 8)

  ;; f(x) = e^x - 1.5 - atan(x)
  ;; f'(x) = e^x - 1/(1 + x^2)
  ;; TODO multiple arguments for d-, d+, d*, etc...
  (let [f (fn [x] (d- (d- (dexp x) 1.5)
                     (datan x)))
        f' (derivative-F f)]
    (f' 0) => 0.0
    (f' 1) => 2.218281828459045
    (f' 2) => 7.18905609893065))

(facts "Derivatives with a square root"
  (let [f (fn [x] (dsqrt x))
        f' (derivative-F f)]
    (f' 0) => (throws java.lang.ArithmeticException "Divide by zero")
    (f' 1) => (roughly 1/2)
    (f' 2) => 0.35355339059327373
    (f' 4) => (roughly 1/4)))

(facts "Derivatives of trigonometric functions"
  (let [f (fn [x] (dsin x))
        f' (derivative-F f)]
    (f' 0) => 1.0
    (f' (/ Math/PI 2)) => (roughly 0 0.00001)
    (f' Math/PI) => -1.0)

  (let [f (fn [x] (dcos x))
        f' (derivative-F f)]
    (f' 0) => 0.0
    (f' (/ Math/PI 2)) => 1.0
    (f' Math/PI) => (roughly 0.0 0.00001)
    (f' (* 3 (/ Math/PI 2))) => -1.0)

  (let [f (fn [x] (dtan x))
        f' (derivative-F f)]
    (f' 0) => 1.0
    (f' Math/PI) => 1.0
    (f' (/ Math/PI 2)) => 2.6670937881135714E32))

(facts "Computing derivatives let's us solve easily using Newton's method"
  (let [f (fn [x] (d- (d- (dexp x) 1.5)
                     (datan x)))
        f' (derivative-F f)
        convergent? (fn [{:keys [steps error] :or {steps 0
                                                  error 1}}]
                      (or (< error 0.00001)
                          (> steps 20)))
        next-guess (fn [{:keys [x last steps] :or {last 0
                                                  steps 0}}]
                     (let [guess (- x (/ (f x) (f' x)))]
                       {:steps (inc steps)
                        :error (- last guess)
                        :last x
                        :x guess}))]
    (first (drop-while #(not (convergent? %))
                       (iterate #(next-guess %)
                                {:x -7})))
    => {:error 1.8005508195528819E-9
        :last -14.101269772739967
        :steps 7
        :x -14.101269772739967}))

(facts "gradients"
  ;; f(x,y) = x^2 + y
  ;; \del f(x,y) = [2*x, 1]
  (let [f (fn [x] (d+ (d* (nth x 0) (nth x 0))
                     (nth x 1)))
        f' (gradient-vector-F f)]
    (f' [2 3]) => [4 1]
    (f' [6 9]) => [12 1])

  (let [f (fn [x] (d+ (d* (nth x 0) (nth x 0))
                     (dsin (nth x 1))))
        f' (gradient-vector-F f)]
    (f' [2 3]) => [4 -0.9899924966004454]
    (f' [6 9]) => [12 -0.9111302618846769]))
